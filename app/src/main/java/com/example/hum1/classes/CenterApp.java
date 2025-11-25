package com.example.hum1.classes;

/**
 * Класс {@code CenterApp} представляет заявку на регистрацию центра.
 * Содержит информацию о центре и идентификаторе заявки.
 */
public class CenterApp {
    /**
     * Название центра.
     */
    private String center;

    /**
     * Идентификатор заявки на регистрацию.
     */
    private String id_appl;

    /**
     * Конструктор класса {@code CenterApp}.
     *
     * @param center  название центра
     * @param id_appl идентификатор заявки
     */
    public CenterApp(String center, String id_appl) {
        this.center = center;
        this.id_appl = id_appl;
    }

    /**
     * Возвращает название центра.
     *
     * @return название центра
     */
    public String getCenter() {
        return this.center;
    }

    /**
     * Устанавливает название центра.
     *
     * @param center новое название центра
     */
    public void setCenter(String center) {
        this.center = center;
    }

    /**
     * Возвращает идентификатор заявки.
     *
     * @return идентификатор заявки
     */
    public String getId_appl() {
        return this.id_appl;
    }

    /**
     * Устанавливает идентификатор заявки.
     *
     * @param id_appl новый идентификатор заявки
     */
    public void setId_appl(String id_appl) {
        this.id_appl = id_appl;
    }
}
