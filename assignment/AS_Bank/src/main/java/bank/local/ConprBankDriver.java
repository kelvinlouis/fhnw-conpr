/*
 * Copyright (c) 2000-2018 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.local;

/* Simple Server -- not thread safe */

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import bank.Account;
import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;

public class ConprBankDriver implements bank.BankDriver {
  private ConprBank bank = null;

  @Override
  public void connect(String[] args) {
    bank = new ConprBank();
  }

  @Override
  public void disconnect() {
    bank = null;
  }

  @Override
  public bank.Bank getBank() {
    return bank;
  }
}

class ConprBank implements Bank {
  private Map<String, ConprAccount> accounts = new ConcurrentHashMap<>();
  private final Object createLock = new Object();

  @Override
  public Set<String> getAccountNumbers() {
    Set<String> activeAccountNumbers = new HashSet<>();
    for (ConprAccount acc : accounts.values()) {
      if (acc.isActive()) {
        activeAccountNumbers.add(acc.getNumber());
      }
    }
    return activeAccountNumbers;
  }

  @Override
  public String createAccount(String owner) {
    synchronized (createLock) {
      final String id = Integer.toString(accounts.size() + 1);
      final ConprAccount a = new ConprAccount(owner, id);
      accounts.put(a.getNumber(), a);
      return a.getNumber();
    }
  }

  @Override
  public boolean closeAccount(String number) {
    final ConprAccount a = accounts.get(number);
    if (a != null) {
      return a.passivate();
    }
    return false;
  }

  @Override
  public Account getAccount(String number) {
    return accounts.get(number);
  }

  @Override
  public void transfer(Account from, Account to, double amount)
      throws IllegalArgumentException, IOException, InactiveException, OverdrawException {
    if (from == null || to == null) throw new IllegalArgumentException();

    Account firstLock;
    Account secondLock;

    // Avoid circular references by establishing order
    if (from.getNumber().compareTo(to.getNumber()) < 0) {
      firstLock = from;
      secondLock = to;
    } else {
      firstLock = to;
      secondLock = from;
    }

    synchronized (firstLock) {
      synchronized (secondLock) {
        if (from.isActive() && to.isActive()) {
          from.withdraw(amount);
          to.deposit(amount);
        } else {
          throw new InactiveException();
        }
      }
    }
  }
}

class ConprAccount implements bank.Account {
  private String number;
  private String owner;
  private double balance;
  private boolean active = true;

  ConprAccount(String owner, String id) {
    this.owner = owner;
    this.number = "CONPR_ACC_" + id;
  }

  @Override
  public double getBalance() {
    return balance;
  }

  @Override
  public String getOwner() {
    return owner;
  }

  @Override
  public String getNumber() {
    return number;
  }

  @Override
  public boolean isActive() {
    return active;
  }

  synchronized boolean passivate() {
    if (getBalance() != 0 || !isActive()) {
      return false;
    } else {
      active = false;
      return true;
    }
  }

  @Override
  public synchronized void deposit(double amount) throws InactiveException {
    if (!active)
      throw new InactiveException("account not active");
    if (amount < 0)
      throw new IllegalArgumentException("negative amount");
    balance += amount;
  }

  @Override
  public synchronized void withdraw(double amount) throws InactiveException, OverdrawException {
    if (!active)
      throw new InactiveException("account not active");
    if (amount < 0)
      throw new IllegalArgumentException("negative amount");
    if (balance - amount < 0)
      throw new OverdrawException("account cannot be overdrawn");
    balance -= amount;
  }
}
