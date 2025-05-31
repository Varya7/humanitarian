package com.example.hum1;

/**
 * Класс {@code ListU2} представляет собой модель данных,
 * содержащую одно поле — {@code margin} для подачи заявки.
 */
public class ListU2 {

    /** Поле для подачи заявки. */
    private String margin;

    /**
     * Конструктор класса {@code ListU2}.
     *
     * @param margin Поле для подачи заявки.
     */
    public ListU2(String margin) {
        this.margin = margin;
    }

    /**
     * Возвращает значение поля {@code margin}, для подачи заявки.
     *
     * @return Строковое значение поля для подачи заявки.
     */
    public String getMargin() {
        return margin;
    }

    /**
     * Устанавливает новое значение поля {@code margin}, для подачи заявки.
     *
     * @param margin Новое строковое значение поля для подачи заявки.
     */
    public void setMargin(String margin) {
        this.margin = margin;
    }
}
