package com.example.hum1;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.hum1.auth.ChangePasswordActivity;
import com.example.hum1.classes.ListU;
import com.example.hum1.editdata.EditDataCenterActivity;
import com.example.hum1.editdata.EditDataUserActivity;
import com.example.hum1.editdata.EditListActivity;
import com.example.hum1.editdata.EditListUActivity;
import com.example.hum1.maps.MapActivity;
import com.example.hum1.maps.MapActivityC;
import com.example.hum1.maps.MapActivityU;
import com.example.hum1.views.ViewAppComplete;
import com.example.hum1.views.ViewApplic;
import com.example.hum1.views.ViewApplicC;
import com.example.hum1.views.ViewApplicQR;
import com.example.hum1.views.ViewCenter;
import com.example.hum1.views.ViewCenterApp;
import com.google.firebase.database.DataSnapshot;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.mapview.MapView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExampleInstrumentedTest {

    @Rule
    public ActivityTestRule<ChangePasswordActivity> activityRule = new ActivityTestRule<ChangePasswordActivity>(
            ChangePasswordActivity.class
    ) {
        @Override
        protected Intent getActivityIntent() {
            return new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                    ChangePasswordActivity.class)
                    .putExtra("role", "user");
        }
    };

    @Test
    public void testChangePasswordActivityLaunches() {
        ChangePasswordActivity activity = activityRule.getActivity();

        assert activity.findViewById(R.id.old_password) != null;
        assert activity.findViewById(R.id.new_password) != null;
        assert activity.findViewById(R.id.save) != null;
    }

    @Test
    public void testChangePasswordActivityRoleHandling() {
        ChangePasswordActivity activity = activityRule.getActivity();

        String role = activity.getIntent().getStringExtra("role");
        assert "user".equals(role);
    }

    @Test
    public void testFirebaseUserNotNullHandling() {
        ChangePasswordActivity activity = activityRule.getActivity();
        assert activity != null;
    }



    @Rule
    public ActivityTestRule<EditDataCenterActivity> editDataRule = new ActivityTestRule<EditDataCenterActivity>(
            EditDataCenterActivity.class
    ) {
        @Override
        protected Intent getActivityIntent() {
            return new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                    EditDataCenterActivity.class);
        }
    };

    @Test
    public void testEditDataCenterActivityLaunches() {
        EditDataCenterActivity activity = editDataRule.getActivity();

        assert activity.findViewById(R.id.center_name) != null;
        assert activity.findViewById(R.id.address) != null;
        assert activity.findViewById(R.id.fio) != null;
        assert activity.findViewById(R.id.work_time) != null;
        assert activity.findViewById(R.id.phone_number) != null;
        assert activity.findViewById(R.id.doc) != null;
        assert activity.findViewById(R.id.save) != null;
    }

    @Test
    public void testEditDataCenterActivityFirebaseHandling() {
        EditDataCenterActivity activity = editDataRule.getActivity();

        assert activity != null;
    }

    @Test
    public void testEditDataCenterSaveButtonExists() {
        EditDataCenterActivity activity = editDataRule.getActivity();

        Button saveButton = activity.findViewById(R.id.save);
        assert saveButton != null;
        assert saveButton.isEnabled();
    }


    @Rule
    public ActivityTestRule<EditDataUserActivity> editUserRule =
            new ActivityTestRule<EditDataUserActivity>(EditDataUserActivity.class, false, false) {
                @Override
                protected Intent getActivityIntent() {
                    return new Intent(
                            InstrumentationRegistry.getInstrumentation().getTargetContext(),
                            EditDataUserActivity.class
                    );
                }
            };

    @Test
    public void testEditDataUserActivityLaunches() {
        EditDataUserActivity activity = editUserRule.launchActivity(null);

        EditText fio = activity.findViewById(R.id.fio);
        EditText birth = activity.findViewById(R.id.birth);
        EditText phone = activity.findViewById(R.id.phone_number);
        Button save = activity.findViewById(R.id.save);

        assert fio != null;
        assert birth != null;
        assert phone != null;
        assert save != null;
    }

    @Test
    public void testEditDataUserActivityInitialValuesDoNotCrash() {
        EditDataUserActivity activity = editUserRule.launchActivity(null);

        EditText fio = activity.findViewById(R.id.fio);
        EditText birth = activity.findViewById(R.id.birth);
        EditText phone = activity.findViewById(R.id.phone_number);

        fio.getText();
        birth.getText();
        phone.getText();
    }

    @Test
    public void testShowBirthPickerDialogDoesNotCrash() {
        EditDataUserActivity activity = editUserRule.launchActivity(null);

        activity.runOnUiThread(activity::showBirthPickerDialog);

        assert activity != null;
    }



    @Rule
    public ActivityTestRule<EditListActivity> editListRule =
            new ActivityTestRule<EditListActivity>(EditListActivity.class, false, false) {
                @Override
                protected Intent getActivityIntent() {
                    return new Intent(
                            InstrumentationRegistry.getInstrumentation().getTargetContext(),
                            EditListActivity.class
                    );
                }
            };

    @Test
    public void testEditListActivityLaunchesAndUiExists() {
        EditListActivity activity = editListRule.launchActivity(null);

        Button save = activity.findViewById(R.id.save);
        Button addRow = activity.findViewById(R.id.btn_add_row);
        LinearLayout container = activity.findViewById(R.id.container_fields);
        RecyclerView recycler = activity.findViewById(R.id.recyclerView_list);

        assert save != null;
        assert addRow != null;
        assert container != null;
        assert recycler != null;
    }


    @Test
    public void testEditListActivityAddRowDoesNotCrash() throws Throwable {
        EditListActivity activity = editListRule.launchActivity(null);

        editListRule.runOnUiThread(activity::addRow);

        LinearLayout container = activity.findViewById(R.id.container_fields);
        boolean hasChild = false;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof LinearLayout) {
                hasChild = true;
                break;
            }
        }
        assert hasChild;
    }


    @Rule
    public ActivityTestRule<EditListUActivity> editListURule =
            new ActivityTestRule<EditListUActivity>(EditListUActivity.class, false, false) {
                @Override
                protected Intent getActivityIntent() {
                    return new Intent(
                            InstrumentationRegistry.getInstrumentation().getTargetContext(),
                            EditListUActivity.class
                    );
                }
            };

    @Test
    public void testEditListUActivityLaunchesAndUiExists() {
        EditListUActivity activity = editListURule.launchActivity(null);

        RecyclerView recycler = activity.findViewById(R.id.recyclerView_list);
        assert recycler != null;

        assert activity.findViewById(R.id.btn_add_row) != null;
        assert activity.findViewById(R.id.save) != null;
    }

    @Test
    public void testEditListUActivityAdapterAttached() {
        EditListUActivity activity = editListURule.launchActivity(null);

        RecyclerView recycler = activity.findViewById(R.id.recyclerView_list);
        RecyclerView.Adapter<?> adapter = recycler.getAdapter();
        assert adapter != null;

        int count = adapter.getItemCount();
        assert count >= 0;
    }

    @Test
    public void testEditListUAddNewFieldDoesNotCrash() throws Throwable {
        EditListUActivity activity = editListURule.launchActivity(null);

        editListURule.runOnUiThread(activity::addNewField);

        assert activity != null;
    }


    @Rule
    public ActivityTestRule<MapActivity> mapActivityRule =
            new ActivityTestRule<MapActivity>(MapActivity.class, false, false) {
                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent(
                            InstrumentationRegistry.getInstrumentation().getTargetContext(),
                            MapActivity.class
                    );
                    intent.putExtra("latitude", 55.751244);
                    intent.putExtra("longitude", 37.618423);
                    return intent;
                }
            };


    @Rule
    public ActivityTestRule<MapActivityC> mapActivityCRule =
            new ActivityTestRule<MapActivityC>(MapActivityC.class, false, false) {
                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent(
                            InstrumentationRegistry.getInstrumentation().getTargetContext(),
                            MapActivityC.class
                    );
                    intent.putExtra("center_name", "Test Center");
                    intent.putExtra("address", "Test Address");
                    intent.putExtra("email", "test@example.com");
                    intent.putExtra("password", "password123");
                    intent.putExtra("fio", "Test FIO");
                    intent.putExtra("work_time", "09:00-18:00");
                    intent.putExtra("phone_number", "+1234567890");
                    intent.putExtra("doc", "DOC123");
                    return intent;
                }
            };


    @Rule
    public ActivityTestRule<MapActivityU> mapActivityURule =
            new ActivityTestRule<MapActivityU>(MapActivityU.class, false, false) {
                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent(
                            InstrumentationRegistry.getInstrumentation().getTargetContext(),
                            MapActivityU.class
                    );
                    return intent;
                }
            };

    @Test
    public void testMapActivityU_LaunchesAndUiExists() {
        MapActivityU activity = mapActivityURule.launchActivity(null);
        assertNotNull(activity);
        assertNotNull(activity.findViewById(R.id.mapview));
        assertNotNull(activity.findViewById(R.id.jambut));
    }

    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule =
            new ActivityTestRule<>(MainActivity.class, false, false);

    @Test
    public void testMainActivity_LaunchesAndUiExists() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MainActivity.class
        );
        MainActivity activity = mainActivityRule.launchActivity(intent);
        assertNotNull(activity);

        assertNotNull(activity.findViewById(R.id.center));
        assertNotNull(activity.findViewById(R.id.appl));
        assertNotNull(activity.findViewById(R.id.date));
        assertNotNull(activity.findViewById(R.id.time));
        assertNotNull(activity.findViewById(R.id.recyclerView_list));
        assertNotNull(activity.findViewById(R.id.recyclerView_list2));
    }

    @Test
    public void testMainActivity_ShowDatePickerDialog_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MainActivity.class
        );
        MainActivity activity = mainActivityRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = MainActivity.class.getDeclaredMethod("showDatePickerDialog");
            m.setAccessible(true);
            mainActivityRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMainActivity_ShowTimePickerDialog_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MainActivity.class
        );
        MainActivity activity = mainActivityRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = MainActivity.class.getDeclaredMethod("showTimePickerDialog");
            m.setAccessible(true);
            mainActivityRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMainActivity_LoadListMethods_DoNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MainActivity.class
        );
        MainActivity activity = mainActivityRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method loadListData = MainActivity.class
                    .getDeclaredMethod("loadListData", String.class);
            Method loadListUData = MainActivity.class
                    .getDeclaredMethod("loadListUData", String.class);
            loadListData.setAccessible(true);
            loadListUData.setAccessible(true);

            mainActivityRule.runOnUiThread(() -> {
                try {
                    loadListData.invoke(activity, "testCenterId");
                    loadListUData.invoke(activity, "testCenterId");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    @Rule
    public ActivityTestRule<MainActivity3> mainActivity3Rule =
            new ActivityTestRule<>(MainActivity3.class, false, false);

    @Test
    public void testMainActivity3_LaunchesAndUiExists() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MainActivity3.class
        );
        intent.putExtra("center_name", "Test Center");
        intent.putExtra("id", "testCenterId");
        MainActivity3 activity = mainActivity3Rule.launchActivity(intent);
        assertNotNull(activity);

        assertNotNull(activity.findViewById(R.id.center_name));
        assertNotNull(activity.findViewById(R.id.date));
        assertNotNull(activity.findViewById(R.id.time));
        assertNotNull(activity.findViewById(R.id.appl));
        assertNotNull(activity.findViewById(R.id.recyclerView_list));
        assertNotNull(activity.findViewById(R.id.recyclerView_list2));
    }

    @Test
    public void testMainActivity3_ShowDatePickerDialog_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MainActivity3.class
        );
        intent.putExtra("center_name", "Test Center");
        intent.putExtra("id", "testCenterId");
        MainActivity3 activity = mainActivity3Rule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = MainActivity3.class.getDeclaredMethod("showDatePickerDialog");
            m.setAccessible(true);
            mainActivity3Rule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMainActivity3_ShowTimePickerDialog_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MainActivity3.class
        );
        intent.putExtra("center_name", "Test Center");
        intent.putExtra("id", "testCenterId");
        MainActivity3 activity = mainActivity3Rule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = MainActivity3.class.getDeclaredMethod("showTimePickerDialog");
            m.setAccessible(true);
            mainActivity3Rule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMainActivity3_LoadListMethods_DoNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MainActivity3.class
        );
        intent.putExtra("center_name", "Test Center");
        intent.putExtra("id", "testCenterId");
        MainActivity3 activity = mainActivity3Rule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method loadListData = MainActivity3.class
                    .getDeclaredMethod("loadListData", String.class);
            Method loadListUData = MainActivity3.class
                    .getDeclaredMethod("loadListUData", String.class);
            loadListData.setAccessible(true);
            loadListUData.setAccessible(true);

            mainActivity3Rule.runOnUiThread(() -> {
                try {
                    loadListData.invoke(activity, "testCenterId");
                    loadListUData.invoke(activity, "testCenterId");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Rule
    public ActivityTestRule<QRcodeActivity> qrActivityRule =
            new ActivityTestRule<>(QRcodeActivity.class, false, false);

    @Test
    public void testQRcodeActivity_LaunchesAndUiExists() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                QRcodeActivity.class
        );
        intent.putExtra("id", "TEST_ID_123");
        QRcodeActivity activity = qrActivityRule.launchActivity(intent);
        assertNotNull(activity);

        assertNotNull(activity.findViewById(R.id.idIVQrcode));
        TextView tvId = activity.findViewById(R.id.idTextView);
        assertNotNull(tvId);
        assertEquals("TEST_ID_123", tvId.getText().toString());
    }

    @Test
    public void testQRcodeActivity_GenerateQRCode_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                QRcodeActivity.class
        );
        intent.putExtra("id", "ANOTHER_TEST_ID");
        QRcodeActivity activity = qrActivityRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = QRcodeActivity.class.getDeclaredMethod("generateQRCode", String.class);
            m.setAccessible(true);
            qrActivityRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity, "SAMPLE_TEXT");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testQRcodeActivity_QRImageIsSet() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                QRcodeActivity.class
        );
        intent.putExtra("id", "IMAGE_TEST_ID");
        QRcodeActivity activity = qrActivityRule.launchActivity(intent);
        assertNotNull(activity);

        ImageView qrImage = activity.findViewById(R.id.idIVQrcode);
        assertNotNull(qrImage);
        assertNotNull(qrImage.getDrawable());
    }


    @Rule
    public ActivityTestRule<StatisticPage> statisticPageRule =
            new ActivityTestRule<>(StatisticPage.class, false, false);

    @Test
    public void testStatisticPage_LaunchesAndUiExists() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                StatisticPage.class
        );
        StatisticPage activity = statisticPageRule.launchActivity(intent);
        assertNotNull(activity);

        assertNotNull(activity.findViewById(R.id.tvCenterName));
        assertNotNull(activity.findViewById(R.id.tvTotal));
        assertNotNull(activity.findViewById(R.id.tvReviewing));
        assertNotNull(activity.findViewById(R.id.tvApproved));
        assertNotNull(activity.findViewById(R.id.tvRejected));
        assertNotNull(activity.findViewById(R.id.tvIssued));
        assertNotNull(activity.findViewById(R.id.tvCompletionRate));
        assertNotNull(activity.findViewById(R.id.spinnerTimeRange));
        assertNotNull(activity.findViewById(R.id.pieChart));
        assertNotNull(activity.findViewById(R.id.barChart));
    }

    @Test
    public void testStatisticPage_UpdateStatistics_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                StatisticPage.class
        );
        StatisticPage activity = statisticPageRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method method = StatisticPage.class.getDeclaredMethod("updateStatistics");
            method.setAccessible(true);
            statisticPageRule.runOnUiThread(() -> {
                try {
                    method.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testStatisticPage_UpdateCharts_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                StatisticPage.class
        );
        StatisticPage activity = statisticPageRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method method = StatisticPage.class.getDeclaredMethod("updateCharts");
            method.setAccessible(true);
            statisticPageRule.runOnUiThread(() -> {
                try {
                    method.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testStatisticPage_GetFilteredApplicationsByTimeRange_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                StatisticPage.class
        );
        StatisticPage activity = statisticPageRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method method = StatisticPage.class
                    .getDeclaredMethod("getFilteredApplicationsByTimeRange");
            method.setAccessible(true);
            Object result = method.invoke(activity);
            assertNotNull(result);
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        }
    }


    @Rule
    public ActivityTestRule<UserListActivity> userListActivityRule =
            new ActivityTestRule<>(UserListActivity.class, false, false);

    @Test
    public void testUserListActivity_LaunchesAndUiExists() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                UserListActivity.class
        );
        intent.putExtra("center_name", "Test Center");
        intent.putExtra("address", "Test Address");
        intent.putExtra("email", "test@example.com");
        intent.putExtra("password", "password123");
        intent.putExtra("fio", "Test FIO");
        intent.putExtra("work_time", "09:00-18:00");
        intent.putExtra("phone_number", "+1234567890");
        intent.putExtra("doc", "DOC");
        intent.putExtra("latitude", 55.75);
        intent.putExtra("longitude", 37.61);

        UserListActivity activity = userListActivityRule.launchActivity(intent);
        assertNotNull(activity);

        assertNotNull(activity.findViewById(R.id.btn_register));
        assertNotNull(activity.findViewById(R.id.btn_add_row));
        assertNotNull(activity.findViewById(R.id.container_fields));
    }

    @Test
    public void testUserListActivity_AddRow_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                UserListActivity.class
        );
        intent.putExtra("center_name", "Test Center");
        intent.putExtra("address", "Test Address");
        intent.putExtra("email", "test@example.com");
        intent.putExtra("password", "password123");
        intent.putExtra("fio", "Test FIO");
        intent.putExtra("work_time", "09:00-18:00");
        intent.putExtra("phone_number", "+1234567890");
        intent.putExtra("doc", "DOC");
        intent.putExtra("latitude", 55.75);
        intent.putExtra("longitude", 37.61);

        UserListActivity activity = userListActivityRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = UserListActivity.class.getDeclaredMethod("addRow");
            m.setAccessible(true);
            userListActivityRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Rule
    public ActivityTestRule<ViewAppComplete> viewAppCompleteRule =
            new ActivityTestRule<>(ViewAppComplete.class, false, false);

    @Test
    public void testViewAppComplete_LaunchesAndUiExists() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewAppComplete.class
        );
        intent.putExtra("id", "test_application_id");
        ViewAppComplete activity = viewAppCompleteRule.launchActivity(intent);
        assertNotNull(activity);

        assertNotNull(activity.findViewById(R.id.date));
        assertNotNull(activity.findViewById(R.id.time));
        assertNotNull(activity.findViewById(R.id.email));
        assertNotNull(activity.findViewById(R.id.fio));
        assertNotNull(activity.findViewById(R.id.phone_number));
        assertNotNull(activity.findViewById(R.id.birth));
        assertNotNull(activity.findViewById(R.id.status));
        assertNotNull(activity.findViewById(R.id.comm));
        assertNotNull(activity.findViewById(R.id.recyclerView_list));
        assertNotNull(activity.findViewById(R.id.recyclerView_list2));
    }

    @Test
    public void testViewAppComplete_LoadListData_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewAppComplete.class
        );
        intent.putExtra("id", "test_application_id");
        ViewAppComplete activity = viewAppCompleteRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = ViewAppComplete.class.getDeclaredMethod("loadListData");
            m.setAccessible(true);
            viewAppCompleteRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testViewAppComplete_LoadListU3Data_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewAppComplete.class
        );
        intent.putExtra("id", "test_application_id");
        ViewAppComplete activity = viewAppCompleteRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = ViewAppComplete.class.getDeclaredMethod("loadListU3Data");
            m.setAccessible(true);
            viewAppCompleteRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Rule
    public ActivityTestRule<ViewApplic> viewApplicRule =
            new ActivityTestRule<>(ViewApplic.class, false, false);

    @Test
    public void testViewApplic_LaunchesAndUiExists() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewApplic.class
        );
        intent.putExtra("id", "test_application_id");
        ViewApplic activity = viewApplicRule.launchActivity(intent);
        assertNotNull(activity);

        assertNotNull(activity.findViewById(R.id.center));
        assertNotNull(activity.findViewById(R.id.status));
        assertNotNull(activity.findViewById(R.id.date));
        assertNotNull(activity.findViewById(R.id.time));
        assertNotNull(activity.findViewById(R.id.email));
        assertNotNull(activity.findViewById(R.id.fio));
        assertNotNull(activity.findViewById(R.id.phone_number));
        assertNotNull(activity.findViewById(R.id.birth));
        assertNotNull(activity.findViewById(R.id.comm));
        assertNotNull(activity.findViewById(R.id.qrcode));
        assertNotNull(activity.findViewById(R.id.recyclerView_list));
        assertNotNull(activity.findViewById(R.id.recyclerView_list2));
    }

    @Test
    public void testViewApplic_LoadListData_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewApplic.class
        );
        intent.putExtra("id", "test_application_id");
        ViewApplic activity = viewApplicRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = ViewApplic.class.getDeclaredMethod("loadListData");
            m.setAccessible(true);
            viewApplicRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testViewApplic_LoadListU3Data_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewApplic.class
        );
        intent.putExtra("id", "test_application_id");
        ViewApplic activity = viewApplicRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = ViewApplic.class.getDeclaredMethod("loadListU3Data");
            m.setAccessible(true);
            viewApplicRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Rule
    public ActivityTestRule<ViewApplicC> viewApplicCRule =
            new ActivityTestRule<>(ViewApplicC.class, false, false);


    @Test
    public void testViewApplicC_LoadListData_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewApplicC.class
        );
        intent.putExtra("id", "test_application_id");
        ViewApplicC activity = viewApplicCRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = ViewApplicC.class.getDeclaredMethod("loadListData");
            m.setAccessible(true);
            viewApplicCRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testViewApplicC_LoadListU3Data_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewApplicC.class
        );
        intent.putExtra("id", "test_application_id");
        ViewApplicC activity = viewApplicCRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = ViewApplicC.class.getDeclaredMethod("loadListU3Data");
            m.setAccessible(true);
            viewApplicCRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    @Rule
    public ActivityTestRule<ViewApplicQR> viewApplicQRRule =
            new ActivityTestRule<>(ViewApplicQR.class, false, false);

    @Test
    public void testViewApplicQR_LaunchesAndUiExists() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewApplicQR.class
        );
        intent.putExtra("id", "test_application_id");
        ViewApplicQR activity = viewApplicQRRule.launchActivity(intent);
        assertNotNull(activity);

        assertNotNull(activity.findViewById(R.id.date));
        assertNotNull(activity.findViewById(R.id.time));
        assertNotNull(activity.findViewById(R.id.email));
        assertNotNull(activity.findViewById(R.id.fio));
        assertNotNull(activity.findViewById(R.id.phone_number));
        assertNotNull(activity.findViewById(R.id.birth));
        assertNotNull(activity.findViewById(R.id.status));
        assertNotNull(activity.findViewById(R.id.error));
        assertNotNull(activity.findViewById(R.id.recyclerView_list));
        assertNotNull(activity.findViewById(R.id.recyclerView_list2));
    }

    @Test
    public void testViewApplicQR_UpdateItemQuantities_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewApplicQR.class
        );
        intent.putExtra("id", "test_application_id");
        ViewApplicQR activity = viewApplicQRRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = ViewApplicQR.class.getDeclaredMethod("updateItemQuantities");
            m.setAccessible(true);
            viewApplicQRRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testViewApplicQR_LoadListData_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewApplicQR.class
        );
        intent.putExtra("id", "test_application_id");
        ViewApplicQR activity = viewApplicQRRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = ViewApplicQR.class.getDeclaredMethod("loadListData");
            m.setAccessible(true);
            viewApplicQRRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testViewApplicQR_LoadListU3Data_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewApplicQR.class
        );
        intent.putExtra("id", "test_application_id");
        ViewApplicQR activity = viewApplicQRRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = ViewApplicQR.class.getDeclaredMethod("loadListU3Data");
            m.setAccessible(true);
            viewApplicQRRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Rule
    public ActivityTestRule<ViewCenter> viewCenterRule =
            new ActivityTestRule<>(ViewCenter.class, false, false);

    @Test
    public void testViewCenter_LaunchesAndUiExists() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewCenter.class
        );
        intent.putExtra("id", "test_center_id");
        ViewCenter activity = viewCenterRule.launchActivity(intent);
        assertNotNull(activity);

        assertNotNull(activity.findViewById(R.id.center_name));
        assertNotNull(activity.findViewById(R.id.address));
        assertNotNull(activity.findViewById(R.id.phone_number));
        assertNotNull(activity.findViewById(R.id.email));
        assertNotNull(activity.findViewById(R.id.fio));
        assertNotNull(activity.findViewById(R.id.work_time));
        assertNotNull(activity.findViewById(R.id.doc));
        assertNotNull(activity.findViewById(R.id.appl));
        assertNotNull(activity.findViewById(R.id.route));
        assertNotNull(activity.findViewById(R.id.recyclerView_list));
    }

    @Test
    public void testViewCenter_GetStringAndDoubleValue_DoNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewCenter.class
        );
        intent.putExtra("id", "test_center_id");
        ViewCenter activity = viewCenterRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method getStringValue = ViewCenter.class
                    .getDeclaredMethod("getStringValue", DataSnapshot.class, String.class);
            Method getDoubleValue = ViewCenter.class
                    .getDeclaredMethod("getDoubleValue", DataSnapshot.class, String.class);
            getStringValue.setAccessible(true);
            getDoubleValue.setAccessible(true);

            DataSnapshot snapshot = Mockito.mock(DataSnapshot.class);
            Mockito.when(snapshot.child(Mockito.anyString())).thenReturn(snapshot);
            Mockito.when(snapshot.exists()).thenReturn(false);

            getStringValue.invoke(activity, snapshot, "center_name");
            getDoubleValue.invoke(activity, snapshot, "latitude");
        } catch (Exception e) {
            fail("Reflection/Mokito error: " + e.getMessage());
        }
    }

    @Test
    public void testViewCenter_LoadListData_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewCenter.class
        );
        intent.putExtra("id", "test_center_id");
        ViewCenter activity = viewCenterRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = ViewCenter.class.getDeclaredMethod("loadListData");
            m.setAccessible(true);
            viewCenterRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    @Rule
    public ActivityTestRule<ViewCenterApp> viewCenterAppRule =
            new ActivityTestRule<>(ViewCenterApp.class, false, false);

    @Test
    public void testViewCenterApp_LaunchesAndUiExists() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewCenterApp.class
        );
        intent.putExtra("id", "test_center_id");
        ViewCenterApp activity = viewCenterAppRule.launchActivity(intent);
        assertNotNull(activity);

        assertNotNull(activity.findViewById(R.id.email));
        assertNotNull(activity.findViewById(R.id.fio));
        assertNotNull(activity.findViewById(R.id.work_time));
        assertNotNull(activity.findViewById(R.id.phone_number));
        assertNotNull(activity.findViewById(R.id.statusT));
        assertNotNull(activity.findViewById(R.id.statusF));
        assertNotNull(activity.findViewById(R.id.comm));
        assertNotNull(activity.findViewById(R.id.center_name));
        assertNotNull(activity.findViewById(R.id.address));
        assertNotNull(activity.findViewById(R.id.doc));
        assertNotNull(activity.findViewById(R.id.recyclerView_list));
        assertNotNull(activity.findViewById(R.id.recyclerView_list2));
    }

    @Test
    public void testViewCenterApp_UpdateListUData_DoesNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewCenterApp.class
        );
        intent.putExtra("id", "test_center_id");
        ViewCenterApp activity = viewCenterAppRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method m = ViewCenterApp.class
                    .getDeclaredMethod("updateListUData", List.class);
            m.setAccessible(true);
            List<ListU> testList = new ArrayList<>();
            testList.add(new ListU("Test service"));
            viewCenterAppRule.runOnUiThread(() -> {
                try {
                    m.invoke(activity, testList);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testViewCenterApp_UpdateRecyclerViewHeights_DoNotCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ViewCenterApp.class
        );
        intent.putExtra("id", "test_center_id");
        ViewCenterApp activity = viewCenterAppRule.launchActivity(intent);
        assertNotNull(activity);

        try {
            Method upd1 = ViewCenterApp.class
                    .getDeclaredMethod("updateRecyclerViewHeight");
            Method upd2 = ViewCenterApp.class
                    .getDeclaredMethod("updateRecyclerViewHeight2");
            upd1.setAccessible(true);
            upd2.setAccessible(true);

            viewCenterAppRule.runOnUiThread(() -> {
                try {
                    upd1.invoke(activity);
                    upd2.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);




        }
    }


}
