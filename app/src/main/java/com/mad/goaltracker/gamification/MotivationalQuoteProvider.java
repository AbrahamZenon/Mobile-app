package com.mad.goaltracker.gamification;

import com.mad.goaltracker.models.Quote;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Cung cấp 20 câu danh ngôn tạo động lực bằng tiếng Việt cho GoalTracker.
 * Không phụ thuộc API bên ngoài, không cần Internet hay cơ sở dữ liệu.
 */
public class MotivationalQuoteProvider {

    private static final List<Quote> QUOTES;
    private final Random random;

    static {
        List<Quote> list = new ArrayList<>(20);
        list.add(new Quote("Mỗi bước đi nhỏ hôm nay đều đưa bạn đến gần hơn với mục tiêu lớn ngày mai.", "GoalTracker"));
        list.add(new Quote("Kỷ luật là chiếc cầu nối vững chắc nhất giữa ước mơ và thành tựu.", "GoalTracker"));
        list.add(new Quote("Đừng chờ đợi cơ hội hoàn hảo, hãy bắt đầu từ hôm nay và hoàn thiện trên đường đi.", "GoalTracker"));
        list.add(new Quote("Thành công không phải đích đến một sớm một chiều, mà là thói quen kiên trì mỗi ngày.", "GoalTracker"));
        list.add(new Quote("Hoàn thành một việc nhỏ tốt hơn nhiều lần so với việc lên kế hoạch vĩ đại mà bỏ dở.", "GoalTracker"));
        list.add(new Quote("Hành trình vạn dặm luôn bắt đầu từ một bước chân kiên định.", "GoalTracker"));
        list.add(new Quote("Tập trung vào sự tiến bộ của bản thân mỗi ngày, chứ không phải sự hoàn hảo tức thì.", "GoalTracker"));
        list.add(new Quote("Khi bạn cảm thấy muốn từ bỏ, hãy nhớ lại lý do vì sao bạn đã bắt đầu.", "GoalTracker"));
        list.add(new Quote("Thói quen tích cực bạn xây dựng hôm nay sẽ định hình tương lai của bạn ngày mai.", "GoalTracker"));
        list.add(new Quote("Mỗi ngày mới là một cơ hội để bạn trở thành phiên bản tốt hơn của chính mình.", "GoalTracker"));
        list.add(new Quote("Sự kiên trì biến những điều tưởng chừng không thể thành hiện thực vững chắc.", "GoalTracker"));
        list.add(new Quote("Hãy làm những việc khó khăn khi chúng còn dễ, bắt đầu từ những nhiệm vụ đơn giản nhất.", "GoalTracker"));
        list.add(new Quote("Chiến thắng vĩ đại nhất là chiến thắng chính sự lười biếng và trì hoãn của bản thân.", "GoalTracker"));
        list.add(new Quote("Mục tiêu rõ ràng là kim chỉ nam giúp bạn không bị lạc lối giữa bộn bề cuộc sống.", "GoalTracker"));
        list.add(new Quote("Năng suất không phải là làm nhiều việc cùng lúc, mà là làm đúng việc quan trọng nhất.", "GoalTracker"));
        list.add(new Quote("Hãy để chuỗi ngày nỗ lực của bạn trở thành bằng chứng sống cho ý chí kiên định.", "GoalTracker"));
        list.add(new Quote("Mỗi nhiệm vụ hoàn thành là một viên gạch xây nên sự tự tin và lòng tự hào của bạn.", "GoalTracker"));
        list.add(new Quote("Không có áp lực thì không có kim cương; hãy đón nhận thử thách như cơ hội trưởng thành.", "GoalTracker"));
        list.add(new Quote("Đích đến có thể còn xa, nhưng nhìn lại xem bạn đã đi xa hơn ngày hôm qua rất nhiều.", "GoalTracker"));
        list.add(new Quote("Hôm nay là ngày tốt nhất để hành động, đừng để ngày mai gánh thêm sự tiếc nuối.", "GoalTracker"));

        QUOTES = Collections.unmodifiableList(list);
    }

    public MotivationalQuoteProvider() {
        this(new Random());
    }

    public MotivationalQuoteProvider(Random random) {
        this.random = random != null ? random : new Random();
    }

    /**
     * Lấy toàn bộ danh sách 20 câu tạo động lực (bất biến, không thể bị sửa đổi từ bên ngoài).
     */
    public List<Quote> getAllQuotes() {
        return QUOTES;
    }

    /**
     * Lấy ngẫu nhiên một câu danh ngôn trong danh sách.
     */
    public Quote getRandomQuote() {
        int index = random.nextInt(QUOTES.size());
        return QUOTES.get(index);
    }

    /**
     * Lấy câu danh ngôn theo ngày hiện tại của thiết bị (đảm bảo hiển thị cố định trong ngày).
     */
    public Quote getDailyQuote() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        return getDailyQuote(today);
    }

    /**
     * Lấy câu danh ngôn cố định theo một ngày cụ thể (định dạng yyyy-MM-dd).
     * Thuật toán băm tất định (deterministic): cùng một ngày luôn trả về cùng một câu danh ngôn.
     * Xử lý an toàn khi date là null, chuỗi rỗng hoặc định dạng bất thường.
     */
    public Quote getDailyQuote(String date) {
        String safeDate = date;
        if (safeDate == null || safeDate.trim().isEmpty()) {
            safeDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        }

        // Sử dụng Math.floorMod để luôn ra chỉ số dương trong khoảng [0, QUOTES.size() - 1]
        int hash = safeDate.trim().hashCode();
        int index = Math.floorMod(hash, QUOTES.size());
        return QUOTES.get(index);
    }
}
