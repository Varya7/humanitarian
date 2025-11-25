package com.example.hum1;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static androidx.test.espresso.Espresso.onData;

import android.content.Context;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import android.content.Intent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.hum1.adapters.AppAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.*;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;


import com.example.hum1.adapters.AppAdapterU;
import com.example.hum1.adapters.CenterAppAdapter;
import com.example.hum1.adapters.ListAdapter;
import com.example.hum1.adapters.ListAdapter2;
import com.example.hum1.adapters.ListU2Adapter;
import com.example.hum1.adapters.ListU3Adapter;
import com.example.hum1.adapters.ListUAdapter;
import com.example.hum1.classes.Application;
import com.example.hum1.classes.ApplicationU;
import com.example.hum1.classes.CenterApp;
import com.example.hum1.classes.ListU;
import com.example.hum1.classes.ListU2;
import com.example.hum1.classes.ListU3;
import com.example.hum1.editdata.EditDataCenterActivity;
import com.example.hum1.editdata.EditDataUserActivity;
import com.example.hum1.editdata.EditListActivity;
import com.example.hum1.editdata.EditListUActivity;
import com.example.hum1.views.ViewAppComplete;
import com.example.hum1.views.ViewApplic;
import com.example.hum1.views.ViewApplicC;
import com.example.hum1.views.ViewCenter;
import com.example.hum1.views.ViewCenterApp;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



import static org.junit.Assert.assertEquals;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;

import androidx.test.espresso.contrib.RecyclerViewActions;
import static org.mockito.Mockito.mock;

// Импорты для Firebase Auth
import com.google.firebase.database.ValueEventListener;


@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private Context context;


    private static final String TEST_EMAIL = "testuser@example.com";
    private static final String TEST_PASSWORD = "123456";
    private static final String TEST_APP_ID = "test_application_123";

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private AutoCloseable closeable;

    @Before
    public void initFirebaseAndSignIn() throws Exception {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        FirebaseApp.initializeApp(appContext);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        CountDownLatch signInLatch = new CountDownLatch(1);
        auth.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
                .addOnCompleteListener(task -> signInLatch.countDown());

        signInLatch.await(5, TimeUnit.SECONDS);

        FirebaseUser user = auth.getCurrentUser();
        assertNotNull("User is not signed in", user);

        prepareTestData();
    }


    @Before
    public void setUp() throws InterruptedException {

        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intents.init();

    }

    @After
    public void tearDown() {
        FirebaseAuth.getInstance().signOut();
            Intents.release();

    }






    @Test
    public void testListUAdapter() {
        List<ListU> items = new ArrayList<>();
        items.add(new ListU("Margin1"));

        ListUAdapter adapter = new ListUAdapter(items);

        assertEquals(1, adapter.getItemCount());

        ListUAdapter.ViewHolder viewHolder = adapter.onCreateViewHolder(
                new ViewGroupForTesting(context), 0);

        adapter.onBindViewHolder(viewHolder, 0);

        assertEquals("Margin1", viewHolder.textView.getText().toString());
    }

    @Test
    public void testListU2Adapter() {
        List<ListU2> items = new ArrayList<>();
        items.add(new ListU2("Hint1"));

        ListU2Adapter adapter = new ListU2Adapter(items);

        assertEquals(1, adapter.getItemCount());

        ListU2Adapter.ViewHolder viewHolder = adapter.onCreateViewHolder(
                new ViewGroupForTesting(context), 0);

        adapter.onBindViewHolder(viewHolder, 0);

        assertEquals("Hint1", viewHolder.editText.getHint().toString());
    }



    private static class ViewGroupForTesting extends ViewGroup {
        public ViewGroupForTesting(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {

        }
    }

    private void prepareTestData() throws InterruptedException {
        DatabaseReference ref = database.getReference("Applications").child(TEST_APP_ID);

        Map<String, Object> testData = new HashMap<>();
        testData.put("email", TEST_EMAIL);
        testData.put("fio", "Иван Иванов");
        testData.put("phone_number", "1234567890");
        testData.put("birth", "01.01.1990");
        testData.put("date", "30.05.2025");
        testData.put("time", "10:00");
        testData.put("status", "Одобрено");
        testData.put("comment", "Все в порядке");
        testData.put("center", "Центр 1");

        Map<String, Object> selectedItems = new HashMap<>();
        selectedItems.put("Хлеб", "2");
        selectedItems.put("Молоко", "1");

        Map<String, Object> listU = new HashMap<>();
        listU.put("Допинфо", "Есть дети");

        testData.put("selected_items", selectedItems);
        testData.put("list_u", listU);

        CountDownLatch dataLatch = new CountDownLatch(1);
        ref.setValue(testData).addOnCompleteListener(task -> dataLatch.countDown());
        dataLatch.await(3, TimeUnit.SECONDS);
    }

    @Test
    public void testViewApplicDisplaysCorrectData() throws InterruptedException {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewApplic.class
        );
        intent.putExtra("id", TEST_APP_ID);

        ActivityScenario.launch(intent);

        Thread.sleep(3000);

        Espresso.onView(withId(R.id.email)).check(matches(withText(TEST_EMAIL)));
        Espresso.onView(withId(R.id.fio)).check(matches(withText("Иван Иванов")));
        Espresso.onView(withId(R.id.phone_number)).check(matches(withText("1234567890")));
        Espresso.onView(withId(R.id.birth)).check(matches(withText("01.01.1990")));
        Espresso.onView(withId(R.id.date)).check(matches(withText("30.05.2025")));
        Espresso.onView(withId(R.id.time)).check(matches(withText("10:00")));
        Espresso.onView(withId(R.id.center)).check(matches(withText("Центр 1")));
        Espresso.onView(withId(R.id.status)).check(matches(withText("Одобрено")));
        Espresso.onView(withId(R.id.comm)).check(matches(withText("Все в порядке")));


        Thread.sleep(1000);
    }


    @Test
    public void testViewApplicCDisplaysCorrectData() throws InterruptedException {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewApplicC.class
        );
        intent.putExtra("id", "test_application_123");

        ActivityScenario.launch(intent);

        Thread.sleep(3000);

        Espresso.onView(withId(R.id.email)).check(matches(withText("testuser@example.com")));
        Espresso.onView(withId(R.id.fio)).check(matches(withText("Иван Иванов")));
        Espresso.onView(withId(R.id.phone_number)).check(matches(withText("1234567890")));
        //Espresso.onView(withId(R.id.center)).check(matches(withText("Центр 1")));
        Espresso.onView(withId(R.id.birth)).check(matches(withText("01.01.1990")));
        Espresso.onView(withId(R.id.date)).check(matches(withText("30.05.2025")));
        Espresso.onView(withId(R.id.time)).check(matches(withText("10:00")));
        Espresso.onView(withId(R.id.statusT)).check(matches(withText("Заявка одобрена!")));
        Espresso.onView(withId(R.id.comm)).perform(replaceText("Комментарий от теста"));


        Thread.sleep(1000);
}

    @Test
    public void testViewAppCompleteDisplaysCorrectData() throws InterruptedException {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewAppComplete.class
        );
        intent.putExtra("id", TEST_APP_ID);

        ActivityScenario.launch(intent);

        Thread.sleep(3000);

        Espresso.onView(withId(R.id.email)).check(matches(withText(TEST_EMAIL)));
        Espresso.onView(withId(R.id.fio)).check(matches(withText("Иван Иванов")));
        Espresso.onView(withId(R.id.phone_number)).check(matches(withText("1234567890")));
        Espresso.onView(withId(R.id.birth)).check(matches(withText("01.01.1990")));
        Espresso.onView(withId(R.id.date)).check(matches(withText("30.05.2025")));
        Espresso.onView(withId(R.id.time)).check(matches(withText("10:00")));

        Espresso.onView(withId(R.id.comm)).check(matches(withText("Все в порядке")));
    }

    @Before
    public void disableAnimations() {
        InstrumentationRegistry.getInstrumentation().getUiAutomation()
                .executeShellCommand("settings put global window_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation()
                .executeShellCommand("settings put global transition_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation()
                .executeShellCommand("settings put global animator_duration_scale 0");
    }

    @Test
    public void testViewCenterActivity() throws InterruptedException {
        String testCenterId = "test_center_" + System.currentTimeMillis();
        DatabaseReference centerRef = database.getReference("Users").child(testCenterId);

        Map<String, Object> centerData = new HashMap<>();
        centerData.put("center_name", "Тестовый центр");
        centerData.put("address", "ул. Тестовая, 123");
        centerData.put("phone_number", "+71234567890");
        centerData.put("email", "testcenter@example.com");
        centerData.put("fio", "Иванов Иван Иванович");
        centerData.put("work_time", "09:00-18:00");
        centerData.put("doc", "Документы принимаются");
        centerData.put("latitude", 55.7558);
        centerData.put("longitude", 37.6176);

        Map<String, Object> items = new HashMap<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("name", "Хлеб");
        item1.put("quantity", "10");
        items.put("item1", item1);

        centerData.put("list_c", items);

        CountDownLatch dataLatch = new CountDownLatch(1);
        centerRef.setValue(centerData).addOnCompleteListener(task -> dataLatch.countDown());
        dataLatch.await(3, TimeUnit.SECONDS);

        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewCenter.class
        );
        intent.putExtra("id", testCenterId);

        ActivityScenario<ViewCenter> scenario = ActivityScenario.launch(intent);

        Thread.sleep(3000);

        onView(withId(R.id.center_name)).check(matches(withText("Тестовый центр")));
        onView(withId(R.id.address)).check(matches(withText("ул. Тестовая, 123")));
        onView(withId(R.id.phone_number)).check(matches(withText("+71234567890")));
        onView(withId(R.id.email)).check(matches(withText("testcenter@example.com")));
        onView(withId(R.id.fio)).check(matches(withText("Иванов Иван Иванович")));
        onView(withId(R.id.work_time)).check(matches(withText("09:00-18:00")));
        onView(withId(R.id.doc)).check(matches(withText("Документы принимаются")));

        onView(withId(R.id.recyclerView_list)).check(matches(isDisplayed()));

        centerRef.removeValue();
    }

    @Test
    public void testViewCenterAppActivity() throws InterruptedException {
        String testCenterId = "test_center_" + System.currentTimeMillis();
        DatabaseReference centerRef = database.getReference("Users").child(testCenterId);

        Map<String, Object> centerData = new HashMap<>();
        centerData.put("center_name", "Тестовый центр");
        centerData.put("address", "ул. Тестовая, 123");
        centerData.put("phone_number", "+71234567890");
        centerData.put("email", "testcenter@example.com");
        centerData.put("fio", "Иванов Иван Иванович");
        centerData.put("work_time", "09:00-18:00");
        centerData.put("doc", "Документы принимаются");
        centerData.put("status", "На рассмотрении");

        Map<String, Object> items = new HashMap<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("name", "Хлеб");
        item1.put("quantity", "10");
        items.put("item1", item1);

        Map<String, Object> services = new HashMap<>();
        services.put("service1", "Выдача продуктов");

        centerData.put("list_c", items);
        centerData.put("list_u", services);

        CountDownLatch dataLatch = new CountDownLatch(1);
        centerRef.setValue(centerData).addOnCompleteListener(task -> dataLatch.countDown());
        dataLatch.await(3, TimeUnit.SECONDS);

        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewCenterApp.class
        );
        intent.putExtra("id", testCenterId);

        Thread.sleep(3000);

        onView(withId(R.id.center_name)).check(matches(withText("Тестовый центр")));
        onView(withId(R.id.address)).check(matches(withText("ул. Тестовая, 123")));
        onView(withId(R.id.phone_number)).check(matches(withText("+71234567890")));
        onView(withId(R.id.email)).check(matches(withText("testcenter@example.com")));
        onView(withId(R.id.fio)).check(matches(withText("Иванов Иван Иванович")));
        onView(withId(R.id.work_time)).check(matches(withText("09:00-18:00")));
        onView(withId(R.id.doc)).check(matches(withText("Документы принимаются")));

        centerRef.removeValue();
    }

    @Test
    public void testCenterApplicationsFragment() throws InterruptedException {
        String testCenterName = "Тестовый центр";
        String testAppId = "test_app_" + System.currentTimeMillis();

        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference userRef = database.getReference("Users").child(user.getUid());
        userRef.child("center_name").setValue(testCenterName);

        DatabaseReference appRef = database.getReference("Applications").child(testAppId);
        Map<String, Object> appData = new HashMap<>();
        appData.put("center", testCenterName);
        appData.put("status", "Одобрено");
        appData.put("date", "01.06.2025");
        appData.put("time", "12:00");
        appData.put("email", "test@example.com");
        appData.put("fio", "Тестовый Пользователь");
        appData.put("phone_number", "1234567890");
        appData.put("birth", "01.01.1990");
        appData.put("family_members", "3");
        appData.put("list", "Хлеб, Молоко");
        appData.put("id_appl", testAppId);

        CountDownLatch dataLatch = new CountDownLatch(1);
        appRef.setValue(appData).addOnCompleteListener(task -> dataLatch.countDown());
        dataLatch.await(3, TimeUnit.SECONDS);


        Thread.sleep(3000);

        onView(withId(R.id.spinner)).check(matches(isDisplayed()));
        onView(withId(R.id.scanner)).check(matches(isDisplayed()));
        onView(withId(R.id.list)).check(matches(isDisplayed()));

        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Одобрено"))).perform(click());

        Thread.sleep(3000);

        onView(withId(R.id.list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        intended(hasComponent(ViewApplicC.class.getName()));

        appRef.removeValue();
        userRef.removeValue();
    }
    @Test
    public void testEditDataCenterActivity() throws Exception {
        String testUserId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = database.getReference("Users").child(testUserId);

        CountDownLatch deleteLatch = new CountDownLatch(1);
        userRef.removeValue().addOnCompleteListener(t -> deleteLatch.countDown());
        deleteLatch.await(3, TimeUnit.SECONDS);

        ActivityScenario<EditDataCenterActivity> scenario = ActivityScenario.launch(EditDataCenterActivity.class);
        Thread.sleep(2000);

        onView(withId(R.id.center_name)).check(matches(withText("")));
        onView(withId(R.id.address)).check(matches(withText("")));
        onView(withId(R.id.fio)).check(matches(withText("")));
        onView(withId(R.id.work_time)).check(matches(withText("")));
        onView(withId(R.id.phone_number)).check(matches(withText("")));
        onView(withId(R.id.doc)).check(matches(withText("")));

        String testCenterName = "Тестовый центр " + System.currentTimeMillis();
        onView(withId(R.id.center_name)).perform(replaceText(testCenterName));
        onView(withId(R.id.address)).perform(replaceText("ул. Тестовая, 123"));
        onView(withId(R.id.fio)).perform(replaceText("Иванов Иван Иванович"));
        onView(withId(R.id.work_time)).perform(replaceText("09:00-18:00"));
        onView(withId(R.id.phone_number)).perform(replaceText("+71234567890"));
        onView(withId(R.id.doc)).perform(replaceText("Тестовые требования"));

        onView(withId(R.id.save)).perform(click());

        CountDownLatch latch = new CountDownLatch(1);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                assertEquals(testCenterName, snapshot.child("center_name").getValue(String.class));
                assertEquals("ул. Тестовая, 123", snapshot.child("address").getValue(String.class));
                assertEquals("Иванов Иван Иванович", snapshot.child("fio").getValue(String.class));
                assertEquals("09:00-18:00", snapshot.child("work_time").getValue(String.class));
                assertEquals("+71234567890", snapshot.child("phone_number").getValue(String.class));
                assertEquals("Тестовые требования", snapshot.child("doc").getValue(String.class));
            } else {
                fail("Ошибка чтения данных из Firebase: " + task.getException());
            }
            latch.countDown();
        });

        assertTrue("Данные не сохранились в Firebase", latch.await(10, TimeUnit.SECONDS));

        Thread.sleep(1000);
        assertTrue(scenario.getState() == Lifecycle.State.DESTROYED);

        userRef.removeValue();
    }

    @Test
    public void testEditDataUserActivity() throws Exception {
        String testUserId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = database.getReference("Users").child(testUserId);

        CountDownLatch deleteLatch = new CountDownLatch(1);
        userRef.removeValue().addOnCompleteListener(t -> deleteLatch.countDown());
        deleteLatch.await(3, TimeUnit.SECONDS);

        ActivityScenario<EditDataUserActivity> scenario = ActivityScenario.launch(EditDataUserActivity.class);
        Thread.sleep(2000);
        onView(withId(R.id.fio)).check(matches(withText("")));
        onView(withId(R.id.birth)).check(matches(withText("")));
        onView(withId(R.id.phone_number)).check(matches(withText("")));

        String testFio = "Тестовый Пользователь " + System.currentTimeMillis();
        String testBirth = "01/01/1990";
        String testPhone = "+79991112233";

        onView(withId(R.id.fio)).perform(replaceText(testFio));
        onView(withId(R.id.birth)).perform(replaceText(testBirth));
        onView(withId(R.id.phone_number)).perform(replaceText(testPhone));

        onView(withId(R.id.save)).perform(click());

        CountDownLatch latch = new CountDownLatch(1);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                assertEquals(testFio, snapshot.child("fio").getValue(String.class));
                assertEquals(testBirth, snapshot.child("birth").getValue(String.class));
                assertEquals(testPhone, snapshot.child("phone_number").getValue(String.class));
            } else {
                fail("Ошибка чтения данных из Firebase: " + task.getException());
            }
            latch.countDown();
        });

        assertTrue("Данные не сохранились в Firebase", latch.await(10, TimeUnit.SECONDS));

        Thread.sleep(1000);
        assertTrue(scenario.getState() == Lifecycle.State.DESTROYED);

        userRef.removeValue();
    }

    @Test
    public void testEditListActivity() throws Exception {
        String testUserId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = database.getReference("Users").child(testUserId).child("list_c");

        CountDownLatch deleteLatch = new CountDownLatch(1);
        userRef.removeValue().addOnCompleteListener(t -> deleteLatch.countDown());
        deleteLatch.await(5, TimeUnit.SECONDS);

        ActivityScenario<EditListActivity> scenario = ActivityScenario.launch(EditListActivity.class);
        Thread.sleep(3000);

        onView(withId(R.id.btn_add_row)).perform(click());

        onView(allOf(
                isDescendantOfA(withId(R.id.container_fields)),
                instanceOf(EditText.class),
                withHint("Название")))
                .perform(replaceText("Молоко"), closeSoftKeyboard());

        onView(allOf(
                isDescendantOfA(withId(R.id.container_fields)),
                instanceOf(EditText.class),
                withHint("Количество")))
                .perform(replaceText("5"), closeSoftKeyboard());

        onView(allOf(
                isDescendantOfA(withId(R.id.container_fields)),
                instanceOf(Button.class),
                withText("Сохранить")))
        .perform(click());

        Thread.sleep(2000);

        onView(withId(R.id.save)).perform(click());
        Thread.sleep(2000);
        CountDownLatch checkLatch = new CountDownLatch(1);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot item : snapshot.getChildren()) {
                    Map<String, String> value = (Map<String, String>) item.getValue();
                    if (value != null && "Молоко".equals(value.get("name"))) {
                        assertEquals("5", value.get("quantity"));
                        found = true;
                        break;
                    }
                }
                assertTrue("Элемент 'Молоко' не найден в Firebase. Содержимое: " + snapshot.getValue(), found);
                checkLatch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fail("Ошибка чтения данных: " + error.getMessage());
                checkLatch.countDown();
            }
        });

        assertTrue("Данные не появились в Firebase", checkLatch.await(10, TimeUnit.SECONDS));

        assertEquals(Lifecycle.State.DESTROYED, scenario.getState());

        userRef.removeValue();
    }

    @Test
    public void testEditListUActivity() throws Exception {
        String testUserId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = database.getReference("Users").child(testUserId).child("list_u");

        CountDownLatch deleteLatch = new CountDownLatch(1);
        userRef.removeValue().addOnCompleteListener(t -> deleteLatch.countDown());
        deleteLatch.await(5, TimeUnit.SECONDS);

        ActivityScenario<EditListUActivity> scenario = ActivityScenario.launch(EditListUActivity.class);

        onView(withId(R.id.recyclerView_list))
                .check(matches(hasChildCount(0)));

        onView(withId(R.id.btn_add_row)).perform(click());

        onView(withClassName(is(EditText.class.getName())))
                .inRoot(isDialog())
                .perform(replaceText("Новое поле"), closeSoftKeyboard());

        onView(withText("OK"))
                .inRoot(isDialog())
                .perform(click());

        Thread.sleep(2000);

        onView(allOf(
                withId(android.R.id.text1),
                withText("Новое поле")))
                .check(matches(isDisplayed()));

        onView(withText("Новое поле"))
                .perform(ViewActions.longClick());

        onView(withClassName(is(EditText.class.getName())))
                .inRoot(isDialog())
                .perform(replaceText("Измененное поле"), closeSoftKeyboard());

        onView(withText("OK"))
                .inRoot(isDialog())
                .perform(click());

        Thread.sleep(2000);

        onView(allOf(
                withId(android.R.id.text1),
                withText("Измененное поле")))
                .check(matches(isDisplayed()));

        onView(withText("Измененное поле"))
                .perform(ViewActions.longClick());

        onView(withText("Удалить"))
                .inRoot(isDialog())
                .perform(click());

        Thread.sleep(2000);

        onView(withId(R.id.recyclerView_list))
                .check(matches(hasChildCount(0)));

        onView(withId(R.id.btn_add_row)).perform(click());
        onView(withClassName(is(EditText.class.getName())))
                .inRoot(isDialog())
                .perform(replaceText("Тестовое поле"), closeSoftKeyboard());
        onView(withText("OK"))
                .inRoot(isDialog())
                .perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.save)).perform(click());
        Thread.sleep(2000);

        CountDownLatch checkLatch = new CountDownLatch(1);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> items = new ArrayList<>();
                for (DataSnapshot item : snapshot.getChildren()) {
                    items.add(item.getValue(String.class));
                }
                assertTrue("Элемент не найден в Firebase: " + items, items.contains("Тестовое поле"));
                checkLatch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fail("Ошибка чтения данных: " + error.getMessage());
                checkLatch.countDown();
            }
        });
        assertTrue(checkLatch.await(10, TimeUnit.SECONDS));

        assertEquals(Lifecycle.State.DESTROYED, scenario.getState());

        userRef.removeValue();
    }


}

