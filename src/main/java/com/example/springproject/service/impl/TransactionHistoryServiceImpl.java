package com.example.springproject.service.impl;

import com.example.springproject.dto.request.TransactionHistoryRequest;
import com.example.springproject.dto.request.TransactionRequestEncode;
import com.example.springproject.dto.response.TransactionHistoryResponse;
import com.example.springproject.entity.TransactionHistory;
import com.example.springproject.repository.TransactionHistoryRepository;
import com.example.springproject.service.TransactionHistoryService;
import com.example.springproject.service.base.BaseServiceImpl;
import com.example.springproject.utils.AESEncryptor;
import com.example.springproject.utils.RSAEncryptorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static com.example.springproject.utils.DateUtils.getCurrentDateTimeString;

@Slf4j
public class TransactionHistoryServiceImpl extends BaseServiceImpl<TransactionHistory> implements TransactionHistoryService {
  private final TransactionHistoryRepository repository;
  private final AESEncryptor aesEncryptor;
  private final RSAEncryptorUtils rsaEncryptorUtils;

  public TransactionHistoryServiceImpl(
        TransactionHistoryRepository repository,
        AESEncryptor aesEncryptor,
        RSAEncryptorUtils rsaEncryptorUtils
  ) {
    super(repository);
    this.repository = repository;
    this.aesEncryptor = aesEncryptor;
    this.rsaEncryptorUtils = rsaEncryptorUtils;
  }

  /**
   * Receive encrypted request from Controller and decrypt then save to database
   *
   * @param request TransactionHistoryRequest
   * @return TransactionHistoryResponse
   */
  @Transactional
  @Override
  public TransactionHistoryResponse transactionHistory(TransactionHistoryRequest request) {

    String transactionId = generateTransactionId();
    String currentDateTime = getCurrentDateTimeString();

    TransactionHistory receive = new TransactionHistory(
          transactionId,
          request.getAccountReceive(),
          BigDecimal.ZERO,
          request.getAmount(),
          currentDateTime,
          request.getAmount()
    );
    repository.save(receive);

    TransactionHistory send = new TransactionHistory(
          transactionId,
          request.getAccountSend(),
          request.getAmount().negate(),
          BigDecimal.ZERO,
          currentDateTime,
          request.getAmount()
    );
    repository.save(send);

    return new TransactionHistoryResponse(
          send.getTransactionID(),
          receive.getAccount(),
          receive.getInDebt(),
          receive.getHave(),
          send.getAccount(),
          send.getInDebt(),
          send.getHave(),
          currentDateTime
    );
  }

  /**
   * Encrypt request
   *
   * @param request TransactionHistoryRequest
   * @return TransactionRequestEncode
   */
  @Override
  public TransactionRequestEncode encrypt(TransactionHistoryRequest request) {
    return new TransactionRequestEncode(
          rsaEncryptorUtils.encrypt(request.getAccountReceive()),
          rsaEncryptorUtils.encrypt(request.getAccountSend()),
          rsaEncryptorUtils.encrypt(rsaEncryptorUtils.convertBigDecimalToString(request.getAmount()))
    );
  }

  /**
   * Decrypt Request from Controller
   *
   * @param requestEncode requestEncode
   * @return TransactionHistoryRequest
   */
  @Override
  public TransactionHistoryRequest decryptRequest(TransactionRequestEncode requestEncode) {
    return new TransactionHistoryRequest(
          rsaEncryptorUtils.decrypt(requestEncode.getAccountReceive()),
          rsaEncryptorUtils.decrypt(requestEncode.getAccountSend()),
          rsaEncryptorUtils.convertStringToBigDecimal(rsaEncryptorUtils.decrypt(requestEncode.getAmount()))
    );
  }

  /**
   * Random TransactionId
   *
   * @return
   */

  private String generateTransactionId() {
    return UUID.randomUUID().toString();
  }
}
