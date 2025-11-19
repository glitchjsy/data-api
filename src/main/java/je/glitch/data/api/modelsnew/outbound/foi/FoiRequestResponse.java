package je.glitch.data.api.modelsnew.outbound.foi;

import lombok.Data;

@Data
public class FoiRequestResponse {
    private final int id;
    private final String publishDate;
    private final String title;
    private final String producer;
    private final String author;
    private final String requestText;
    private final String responseText;
}
