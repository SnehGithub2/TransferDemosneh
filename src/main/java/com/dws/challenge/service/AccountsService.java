package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
@Slf4j
@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  @Autowired
  private final NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository,
                         EmailNotificationService emailNotificationService) {
    this.accountsRepository = accountsRepository;
      this.notificationService = emailNotificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
  @Transactional
  public void transferMoney(String fromAccount, String toAccount, BigDecimal amount) {
    Account a1 = this.accountsRepository.findById(fromAccount);
    Account a2 = this.accountsRepository.findById(toAccount);
    if(a1.getBalance().compareTo(amount)<0){
      throw new InsufficientBalanceException("Insufficient Balance. ");
    }
    else {
      log.info("Transfer initiated..");
      a1.setBalance(a1.getBalance().subtract(amount));
      a2.setBalance(a2.getBalance().add(amount));
      boolean debitSuccess = this.accountsRepository.save(a1);
      boolean creditSuccess = this.accountsRepository.save(a2);
      if (debitSuccess){
         if (creditSuccess) {
          log.info("Transfer Completed");
          notificationService.notifyAboutTransfer(a1,"Account debited");
          notificationService.notifyAboutTransfer(a2,"Account credited");
        }
      }
      log.info("Transfer Completed.");
    }
  }
}
