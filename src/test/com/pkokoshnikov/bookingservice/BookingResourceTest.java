package com.pkokoshnikov.bookingservice;

import com.pkokoshnikov.bookingservice.injection.ApplicationBinder;
import com.pkokoshnikov.bookingservice.model.BookingBatch;
import com.pkokoshnikov.bookingservice.model.BookingItem;
import com.pkokoshnikov.bookingservice.model.BookingItemResponse;
import com.pkokoshnikov.bookingservice.model.GroupByDayBookingItem;
import com.pkokoshnikov.bookingservice.util.formatters.Constants;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.*;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * User: pako1113
 * Date: 13.07.15
 */

public class BookingResourceTest extends JerseyTest {

    private final SimpleDateFormat requestTimeSubmissionFormatter = new SimpleDateFormat(Constants.REQUEST_SUBMISSION_FORMAT);
    private final SimpleDateFormat meetingStartDateFormatter = new SimpleDateFormat(Constants.MEETING_START_FORMAT);

    @Test
    public void statusTest() {
        Response response = target("/booking/status").request().get();
        System.out.println(response);
    }

    @Test
    public void processTest() throws ParseException {
        BookingItem bookingItem1 = new BookingItem(requestTimeSubmissionFormatter.parse("2011-03-17 10:17:06"), meetingStartDateFormatter.parse("2011-03-21 09:00"),
                "EMP001", 2);
        BookingItem bookingItem2 = new BookingItem(requestTimeSubmissionFormatter.parse("2011-03-17 10:18:06"), meetingStartDateFormatter.parse("2011-03-21 11:00"),
                "EMP002", 2);
        BookingItem bookingItem3 = new BookingItem(requestTimeSubmissionFormatter.parse("2011-03-17 10:19:06"), meetingStartDateFormatter.parse("2011-03-22 14:00"),
                "EMP003", 3);
        BookingBatch bookingBatch = new BookingBatch("0900", "1700", new BookingItem[]{bookingItem1, bookingItem2, bookingItem3});
        Entity<BookingBatch> entity = Entity.entity(bookingBatch, MediaType.APPLICATION_JSON_TYPE);

        Response response = target("/booking/process").request(MediaType.APPLICATION_JSON_TYPE).post(entity);
        List<GroupByDayBookingItem> responseBody = response.readEntity(new GenericType<List<GroupByDayBookingItem>>(){});

        assertEquals(responseBody.size(),2);
        assertEquals(responseBody.get(0).getBookingItems().get(0), new BookingItemResponse(bookingItem1));
        assertEquals(responseBody.get(0).getBookingItems().get(1), new BookingItemResponse(bookingItem2));
        assertEquals(responseBody.get(1).getBookingItems().get(0), new BookingItemResponse(bookingItem3));
    }

    @Override
    protected Application configure() {
        return new ResourceConfig() {
            {
                register(new ApplicationBinder());
                register(BookingResource.class);
                enable(TestProperties.LOG_TRAFFIC);
                enable(TestProperties.DUMP_ENTITY);
            }
        };
    }
}
