package com.example.hum1;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.hum1.classes.Application;
import com.example.hum1.classes.ApplicationU;
import com.example.hum1.classes.Center;
import com.example.hum1.classes.CenterApp;
import com.example.hum1.classes.ListU;
import com.example.hum1.classes.ListU2;
import com.example.hum1.classes.ListU3;

@RunWith(JUnit4.class)
public class ExampleUnitTest {

    @Test
    public void testApplicationClass() {
        Application app = new Application("1", "2023-01-01", "10:00", "test@example.com",
                "Иванов Иван", "+79991112233", "01.01.1980",
                "3", "Хлеб, Молоко", "Новая");

        assertEquals("1", app.getId_appl());
        assertEquals("2023-01-01", app.getDate());
        assertEquals("Иванов Иван", app.getFIO());
        assertEquals("Хлеб, Молоко", app.getList());

        app.setId_appl("2");
        assertEquals("2", app.getId_appl());
    }

    @Test
    public void testApplicationUClass() {
        ApplicationU appU = new ApplicationU("2", "2023-01-02", "11:00", "user@example.com",
                "Петров Петр", "+79994445566", "02.02.1985",
                "2", "Мясо, Овощи", "Одобрена", "Центр помощи 1");

        assertEquals("2", appU.getId_appl());
        assertEquals("Центр помощи 1", appU.getCenter());
        assertEquals("Одобрена", appU.getStatus());

        appU.setCenter("Новый центр");
        assertEquals("Новый центр", appU.getCenter());
    }

    @Test
    public void testCenterClass() {
        Center center = new Center("c1", "Центр помощи", "ул. Ленина 1", "center@example.com",
                "Иванов Иван", "9:00-18:00", "+79998887766", "Продукты, Одежда");

        assertEquals("c1", center.getId());
        assertEquals("Центр помощи", center.getCenter_name());
        assertEquals("ул. Ленина 1", center.getAddress());

        center.setAddress("ул. Пушкина 10");
        assertEquals("ул. Пушкина 10", center.getAddress());
    }

    @Test
    public void testCenterAppClass() {
        CenterApp centerApp = new CenterApp("Новый центр", "app123");

        assertEquals("Новый центр", centerApp.getCenter());
        assertEquals("app123", centerApp.getId_appl());

        centerApp.setCenter("Обновленный центр");
        assertEquals("Обновленный центр", centerApp.getCenter());
    }

    @Test
    public void testListU2Class() {
        ListU2 listU2 = new ListU2("Test Margin");
        assertEquals("Test Margin", listU2.getMargin());

        listU2.setMargin("New Margin");
        assertEquals("New Margin", listU2.getMargin());
    }

    @Test
    public void testListUClass() {
        ListU listU = new ListU("Test Margin");
        assertEquals("Test Margin", listU.getMargin());

        listU.setMargin("New Margin");
        assertEquals("New Margin", listU.getMargin());
    }

    @Test
    public void testListU3Class() {
        ListU3 listU3 = new ListU3("Test Label", "10");
        assertEquals("Test Label", listU3.getLabel());
        assertEquals("10", listU3.getValue());

        listU3.setLabel("New Label");
        listU3.setValue("20");
        assertEquals("New Label", listU3.getLabel());
        assertEquals("20", listU3.getValue());
    }
}