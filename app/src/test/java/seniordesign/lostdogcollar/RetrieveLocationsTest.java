package seniordesign.lostdogcollar;

import org.junit.Before;
import org.junit.Test;

import seniordesign.lostdogcollar.async.RetrieveFromServerAsyncTask;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RetrieveLocationsTest {

    String correctResponse;

    @Before
    public void setCorrectResponse() {

    }

    @Test
    public void SendRequestTest() {
        String message = "GET_RECORDS 0 2 ";
        RetrieveFromServerAsyncTask rfsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                assertThat(response.equals(correctResponse), is(true));
            }
        });
        rfsat.execute(message);
    }

}