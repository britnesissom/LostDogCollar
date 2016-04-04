package seniordesign.lostdogcollar;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import seniordesign.lostdogcollar.listeners.OnSendResponseListener;

@RunWith(RobolectricGradleTestRunner.class)
public class AsyncTaskRetrieveTest extends ApplicationTestCase<Application> {
    public AsyncTaskRetrieveTest() {
        super(Application.class);
    }

    private String correctLocation;
    private String correctSafezone;

    @Override
    public void setUp() {
        correctLocation = "Location:(30.318384,-97.716075) Battery:5% Sleep Time:100s Fri Apr 01 2016 11:36:03 AM  ";
        correctSafezone = "";
    }

    @Test
    public void testRetrieveLocationCollar0() {
        String message = "GET_RECORDS 0 1 ";
        RetrieveFromServerAsyncTask rfsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                assertEquals(correctLocation, response);
            }
        });
        rfsat.execute(message);
    }

    @Test
    public void testRetrieveSafezoneCollar0() {
        String message = "GET_SAFEZONES 0 \r\n";
        RetrieveFromServerAsyncTask rfsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                assertEquals(correctSafezone, response);
            }
        });
        rfsat.execute(message);
    }

    @Test
    public void testRetrieveLocationCollar1() {
        String message = "GET_RECORDS 1 1 ";
        RetrieveFromServerAsyncTask rfsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                assertEquals(correctLocation, response);
            }
        });
        rfsat.execute(message);
    }

    @Test
    public void testRetrieveSafezoneCollar1() {
        String message = "GET_SAFEZONES 1 \r\n";
        RetrieveFromServerAsyncTask rfsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                assertEquals(correctSafezone, response);
            }
        });
        rfsat.execute(message);
    }

}