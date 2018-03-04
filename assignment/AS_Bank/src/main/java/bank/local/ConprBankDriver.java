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

  @Override
  public Set<String> getAccountNumbers() {
    Set<String> activeAccountNumbers = Collections.synchronizedSet(new HashSet<>());
    for (ConprAccount acc : accounts.values()) {
      if (acc.isActive()) {
        activeAccountNumbers.add(acc.getNumber());
      }
    }
    return activeAccountNumbers;
  }

  @Override
  public String createAccount(String owner) {
    final ConprAccount a = new ConprAccount(owner);
    accounts.put(a.getNumber(), a);
    return a.getNumber();
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
  public void transfer(bank.Account from, bank.Account to, double amount)
      throws IOException, InactiveException, OverdrawException {

    if (from.isActive() && to.isActive()) {
      from.withdraw(amount);
      to.deposit(amount);
    } else {
      throw new InactiveException();
    }
  }
}

class ConprAccount implements bank.Account {
  private static int id = 0;

  private String number;
  private String owner;
  private double balance;
  private boolean active = true;
  private final Object accountLock = new Object();

  ConprAccount(String owner) {
    this.owner = owner;
    this.number = "CONPR_ACC_" + id++;
  }

  @Override
  public double getBalance() {
    synchronized (accountLock) {
      return balance;
    }
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
    synchronized (accountLock) {
      return active;
    }
  }

  boolean passivate() {
    synchronized (accountLock) {
      if (getBalance() != 0 || !isActive()) {
        return false;
      } else {
        active = false;
        return true;
      }
    }
  }

  @Override
  public void deposit(double amount) throws InactiveException {
    synchronized (accountLock) {
      if (!active)
        throw new InactiveException("account not active");
      if (amount < 0)
        throw new IllegalArgumentException("negative amount");
      balance += amount;
    }
  }

  @Override
  public void withdraw(double amount) throws InactiveException, OverdrawException {
    synchronized (accountLock) {
      if (!active)
        throw new InactiveException("account not active");
      if (amount < 0)
        throw new IllegalArgumentException("negative amount");
      if (balance - amount < 0)
        throw new OverdrawException("account cannot be overdrawn");
      balance -= amount;
    }
  }
}
