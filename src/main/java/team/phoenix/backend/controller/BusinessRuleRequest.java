package team.phoenix.backend.controller;

// DTO de entrada para criação de regra de negócio via linguagem natural
public record BusinessRuleRequest(String rawText, String yearMonth) {}
