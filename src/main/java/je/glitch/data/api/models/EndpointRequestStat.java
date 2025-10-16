package je.glitch.data.api.models;

public class EndpointRequestStat {
    private final String path;
    private final long total;

    public EndpointRequestStat(String path, long total) {
        this.path = path;
        this.total = total;
    }

    public String getPath() { return path; }
    public long getTotal() { return total; }
}
