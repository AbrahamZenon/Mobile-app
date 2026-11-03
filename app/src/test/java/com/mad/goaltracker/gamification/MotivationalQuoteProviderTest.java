package com.mad.goaltracker.gamification;

import com.mad.goaltracker.models.Quote;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Kiểm thử đơn vị cho MotivationalQuoteProvider.
 */
public class MotivationalQuoteProviderTest {

    private MotivationalQuoteProvider provider;

    @Before
    public void setUp() {
        provider = new MotivationalQuoteProvider();
    }

    @Test
    public void test01_Exactly20Quotes() {
        List<Quote> quotes = provider.getAllQuotes();
        assertNotNull(quotes);
        assertEquals("Must provide exactly 20 motivational quotes", 20, quotes.size());
    }

    @Test
    public void test02_NoNullOrEmptyContentOrAuthor() {
        List<Quote> quotes = provider.getAllQuotes();
        for (int i = 0; i < quotes.size(); i++) {
            Quote q = quotes.get(i);
            assertNotNull("Quote at index " + i + " must not be null", q);
            assertNotNull("Quote content at index " + i + " must not be null", q.getContent());
            assertFalse("Quote content at index " + i + " must not be empty", q.getContent().trim().isEmpty());
            assertNotNull("Quote author at index " + i + " must not be null", q.getAuthor());
            assertFalse("Quote author at index " + i + " must not be empty", q.getAuthor().trim().isEmpty());
        }
    }

    @Test
    public void test03_NoDuplicateQuoteContent() {
        List<Quote> quotes = provider.getAllQuotes();
        Set<String> uniqueContents = new HashSet<>();
        for (Quote q : quotes) {
            boolean added = uniqueContents.add(q.getContent().trim());
            assertTrue("Duplicate quote content detected: " + q.getContent(), added);
        }
        assertEquals(20, uniqueContents.size());
    }

    @Test
    public void test04_GetRandomQuoteReturnsValidQuote() {
        for (int i = 0; i < 50; i++) {
            Quote q = provider.getRandomQuote();
            assertNotNull("getRandomQuote() must return non-null quote", q);
            assertNotNull(q.getContent());
            assertFalse(q.getContent().isEmpty());
        }
    }

    @Test
    public void test05_GetDailyQuoteIsDeterministicForSameDate() {
        String testDate = "2026-11-03";
        Quote q1 = provider.getDailyQuote(testDate);
        Quote q2 = provider.getDailyQuote(testDate);
        Quote q3 = provider.getDailyQuote(testDate);

        assertNotNull(q1);
        assertEquals("Daily quote must be deterministic for the same date", q1.getContent(), q2.getContent());
        assertEquals("Daily quote must be deterministic for the same date", q1.getContent(), q3.getContent());
    }

    @Test
    public void test06_GetDailyQuoteDifferentDatesCanVary() {
        // Kiểm tra phân bổ qua 10 ngày khác nhau
        Set<String> distinctQuotes = new HashSet<>();
        for (int day = 1; day <= 10; day++) {
            Quote q = provider.getDailyQuote(String.format("2026-11-%02d", day));
            assertNotNull(q);
            distinctQuotes.add(q.getContent());
        }
        // Qua 10 ngày phải có ít nhất 2 câu khác nhau
        assertTrue("Different dates should produce varying quotes", distinctQuotes.size() > 1);
    }

    @Test
    public void test07_GetDailyQuoteHandlesNullOrEmptySafely() {
        Quote qNull = provider.getDailyQuote(null);
        assertNotNull("Should safely handle null date", qNull);
        assertNotNull(qNull.getContent());

        Quote qEmpty = provider.getDailyQuote("");
        assertNotNull("Should safely handle empty date", qEmpty);
        assertNotNull(qEmpty.getContent());

        Quote qBlank = provider.getDailyQuote("   ");
        assertNotNull("Should safely handle blank date", qBlank);
        assertNotNull(qBlank.getContent());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test08_GetAllQuotesReturnsUnmodifiableList() {
        List<Quote> quotes = provider.getAllQuotes();
        // Cố tình thay đổi danh sách trả về -> phải ném UnsupportedOperationException
        quotes.add(new Quote("New quote", "Author"));
    }
}
