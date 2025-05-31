package com.example.hum1;

/**
 * Класс {@code ListU3} представляет пару значений: {@code label} (вещь) и {@code value} (количество).
 * Может использоваться, например, для отображения или хранения структурированных данных
 * в виде "Название поля" — "Значение".
 */
public class ListU3 {

    /** Название вещи. */
    private String label;

    /** Количество. */
    private String value;

    /**
     * Конструктор класса {@code ListU3}.
     *
     * @param label Название вещи.
     * @param value Количество.
     */
    public ListU3(String label, String value) {
        this.label = label;
        this.value = value;
    }

    /**
     * Возвращает название вещи.
     *
     * @return Строка с названием вещи.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Устанавливает новую вещь.
     *
     * @param label Новое название вещи.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Возвращает количество вещей.
     *
     * @return Строковое значение.
     */
    public String getValue() {
        return value;
    }

    /**
     * Устанавливает новое количество.
     *
     * @param value Новое строковое значение количества.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
