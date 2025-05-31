package com.example.hum1;

/**
 * Класс, представляющий центр (организацию) в системе.
 * Содержит информацию о центре, включая контактные данные, адрес и список предоставляемых услуг.
 */
public class Center {
    private String id, email, center_name, address, phone_number, fio, work_time, list;

    /**
     * Конструктор для создания объекта центра.
     *
     * @param id уникальный идентификатор центра
     * @param center_name название центра
     * @param address адрес центра
     * @param email электронная почта центра
     * @param fio ФИО ответственного лица
     * @param work_time время работы центра
     * @param phone_number контактный телефон
     * @param list перечень предоставляемых товаров
     */
    public Center(String id, String center_name, String address, String email,
                  String fio, String work_time, String phone_number, String list) {
        this.id = id;
        this.center_name = center_name;
        this.email = email;
        this.address = address;
        this.phone_number = phone_number;
        this.fio = fio;
        this.work_time = work_time;
        this.list = list;
    }

    /**
     * Возвращает идентификатор центра.
     *
     * @return уникальный идентификатор
     */
    public String getId() {
        return this.id;
    }

    /**
     * Устанавливает идентификатор центра.
     *
     * @param id новый идентификатор
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Возвращает название центра.
     *
     * @return название центра
     */
    public String getCenter_name() {
        return this.center_name;
    }

    /**
     * Устанавливает название центра.
     *
     * @param center_name новое название
     */
    public void setCenter_name(String center_name) {
        this.center_name = center_name;
    }

    /**
     * Возвращает адрес центра.
     *
     * @return адрес центра
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Устанавливает адрес центра.
     *
     * @param address новый адрес
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Возвращает электронную почту центра.
     *
     * @return email центра
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Устанавливает электронную почту центра.
     *
     * @param email новый email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Возвращает ФИО ответственного лица.
     *
     * @return ФИО ответственного
     */
    public String getFIO() {
        return this.fio;
    }

    /**
     * Устанавливает ФИО ответственного лица.
     *
     * @param fio новые ФИО
     */
    public void setFIO(String fio) {
        this.fio = fio;
    }

    /**
     * Возвращает время работы центра.
     *
     * @return время работы
     */
    public String getWorkTime() {
        return this.work_time;
    }

    /**
     * Устанавливает время работы центра.
     *
     * @param work_time новое время работы
     */
    public void setWork_time(String work_time) {
        this.work_time = work_time;
    }

    /**
     * Возвращает контактный телефон центра.
     *
     * @return телефон центра
     */
    public String getPhone_number() {
        return this.phone_number;
    }

    /**
     * Устанавливает контактный телефон центра.
     *
     * @param phone_number новый телефон
     */
    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    /**
     * Возвращает список услуг/товаров центра.
     *
     * @return перечень услуг
     */
    public String getList() {
        return this.list;
    }

    /**
     * Устанавливает список услуг/товаров центра.
     *
     * @param list новый перечень услуг
     */
    public void setList(String list) {
        this.list = list;
    }
}