package com.example.hum1.classes;

/**
 * Класс ListU представляет простой объект с одним полем {@code margin}.
 * Может использоваться для хранения и передачи строки поля для подачи заявки (margin).
 */
public class ListU {

    /** Значение поля. */
    private String margin;

    /**
     * Конструктор класса ListU.
     *
     * @param margin Значение поля.
     */
    public ListU(String margin) {
        this.margin = margin;
    }

    /**
     * Возвращает значение поля.
     *
     * @return Строковое значение поля.
     */
    public String getMargin() {
        return margin;
    }

    /**
     * Устанавливает новое значение поля.
     *
     * @param margin Новое строковое значение поля.
     */
    public void setMargin(String margin) {
        this.margin = margin;
    }
}
