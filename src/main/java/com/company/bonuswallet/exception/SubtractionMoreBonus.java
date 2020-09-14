package com.company.bonuswallet.exception;

public class SubtractionMoreBonus extends Exception{
    @Override
    public String getMessage() {
        return "Вычитаемая сумма превышает количество бонусов";
    }
}
