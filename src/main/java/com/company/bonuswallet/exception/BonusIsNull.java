package com.company.bonuswallet.exception;

public class BonusIsNull extends Exception{
    @Override
    public String getMessage() {
        return "У данного пользователя 0 бонусов";
    }
}
