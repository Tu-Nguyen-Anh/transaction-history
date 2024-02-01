package com.example.springproject.service;

import com.example.springproject.dto.request.TransactionHistoryRequest;
import com.example.springproject.dto.request.TransactionRequestEncode;
import com.example.springproject.dto.response.TransactionHistoryResponse;

public interface TransactionHistoryService {
  TransactionHistoryResponse transactionHistory(TransactionHistoryRequest request);

  TransactionRequestEncode encrypt(TransactionHistoryRequest request);

  TransactionHistoryRequest decryptRequest(TransactionRequestEncode requestEncode);

  }
