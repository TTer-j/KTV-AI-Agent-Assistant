package com.ktvai.assistant.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktvai.assistant.entity.Song;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MusicApiClient {

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Song> neteaseExactCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> CURATED_NETEASE_IDS = Map.ofEntries(
            Map.entry("告白气球::周杰伦", 418603077L),
            Map.entry("晴天::周杰伦", 186016L),
            Map.entry("孤勇者::陈奕迅", 1901371647L)
    );

    private static final List<SongSeed> SONG_LIBRARY = List.of(
            new SongSeed("稻香", "周杰伦", "流行", "KTV热门,朋友聚会,车载路上听,慢歌", "华语 国语 治愈 慢歌", 9800),
            new SongSeed("七里香", "周杰伦", "流行", "KTV热门,经典老歌,车载路上听,慢歌", "华语 国语 青春 慢歌", 9700),
            new SongSeed("告白气球", "周杰伦", "情歌", "浪漫约会,情侣对唱,告白,生日派对", "华语 国语 告白 甜歌", 9600),
            new SongSeed("晴天", "周杰伦", "流行", "经典老歌,朋友聚会,车载路上听", "华语 国语 青春 慢歌", 9500),
            new SongSeed("夜曲", "周杰伦", "R&B", "KTV热门,慢歌", "华语 国语 慢歌", 9300),
            new SongSeed("十年", "陈奕迅", "情歌", "KTV热门,经典老歌,慢歌", "粤语 国语 伤感 慢歌", 9400),
            new SongSeed("K歌之王", "陈奕迅", "粤语", "KTV热门,朋友聚会,粤语", "粤语 很火 麦霸", 9300),
            new SongSeed("浮夸", "陈奕迅", "粤语", "KTV热门,朋友聚会,粤语", "粤语 高音 气氛", 9200),
            new SongSeed("富士山下", "陈奕迅", "粤语", "经典老歌,粤语,慢歌", "粤语 情歌 慢歌", 9100),
            new SongSeed("红玫瑰", "陈奕迅", "流行", "KTV热门,慢歌", "华语 情歌 慢歌", 9000),
            new SongSeed("吻别", "张学友", "经典", "经典老歌,KTV热门,公司团建暖场", "华语 国语 老歌", 9200),
            new SongSeed("一千个伤心的理由", "张学友", "经典", "经典老歌,KTV热门", "华语 国语 老歌 伤感", 9000),
            new SongSeed("慢慢", "张学友", "情歌", "经典老歌,慢歌", "华语 国语 慢歌", 8500),
            new SongSeed("如果这都不算爱", "张学友", "情歌", "KTV热门,慢歌", "华语 国语 情歌", 8400),
            new SongSeed("江南", "林俊杰", "流行", "KTV热门,经典老歌", "华语 国语", 9100),
            new SongSeed("小酒窝", "林俊杰", "情歌", "情侣对唱,浪漫约会,告白", "华语 国语 对唱 甜歌", 9000),
            new SongSeed("曹操", "林俊杰", "流行", "朋友聚会,KTV热门", "华语 国语 热场", 8600),
            new SongSeed("修炼爱情", "林俊杰", "情歌", "KTV热门,慢歌", "华语 国语 情歌 慢歌", 8800),
            new SongSeed("红豆", "王菲", "情歌", "经典老歌,慢歌,浪漫约会", "华语 国语 女声 慢歌", 9000),
            new SongSeed("匆匆那年", "王菲", "流行", "经典老歌,慢歌", "华语 国语 女声", 8700),
            new SongSeed("甜蜜蜜", "邓丽君", "经典", "经典老歌,公司团建暖场,生日派对", "华语 国语 女声 老歌", 8900),
            new SongSeed("月亮代表我的心", "邓丽君", "经典", "经典老歌,情侣对唱,告白", "华语 国语 老歌 情歌", 9100),
            new SongSeed("忘情水", "刘德华", "经典", "经典老歌,KTV热门", "华语 国语 男声", 8800),
            new SongSeed("爱你一万年", "刘德华", "经典", "经典老歌,告白,情侣对唱", "华语 国语 情歌", 8700),
            new SongSeed("温柔", "五月天", "摇滚", "朋友聚会,经典老歌,慢歌", "华语 国语 乐队 慢歌", 8800),
            new SongSeed("倔强", "五月天", "摇滚", "朋友聚会,公司团建暖场", "华语 国语 热血 合唱", 9000),
            new SongSeed("突然好想你", "五月天", "情歌", "KTV热门,慢歌", "华语 国语 乐队 情歌", 8900),
            new SongSeed("恋爱ing", "五月天", "摇滚", "朋友聚会,生日派对,动感舞曲", "华语 国语 嗨 合唱", 8700),
            new SongSeed("遇见", "孙燕姿", "流行", "经典老歌,慢歌,车载路上听", "华语 国语 女声", 9000),
            new SongSeed("绿光", "孙燕姿", "流行", "朋友聚会,动感舞曲", "华语 国语 女声 嗨", 8600),
            new SongSeed("勇气", "梁静茹", "情歌", "告白,情侣对唱,慢歌", "华语 国语 女声 情歌", 9100),
            new SongSeed("可惜不是你", "梁静茹", "情歌", "KTV热门,慢歌", "华语 国语 女声 伤感", 8700),
            new SongSeed("海阔天空", "Beyond", "粤语", "朋友聚会,公司团建暖场,经典老歌,粤语", "粤语 合唱 乐队 老歌", 9500),
            new SongSeed("光辉岁月", "Beyond", "粤语", "朋友聚会,公司团建暖场,粤语", "粤语 合唱 热血", 9300),
            new SongSeed("讲不出再见", "谭咏麟", "粤语", "经典老歌,粤语,慢歌", "粤语 男声 老歌", 8800),
            new SongSeed("喜欢你", "G.E.M.邓紫棋", "粤语", "粤语,告白,情侣对唱", "粤语 女声 情歌", 9000),
            new SongSeed("泡沫", "G.E.M.邓紫棋", "流行", "KTV热门,慢歌", "华语 国语 女声 高音", 9200),
            new SongSeed("孤勇者", "陈奕迅", "流行", "KTV热门,公司团建暖场,朋友聚会", "华语 国语 很火 合唱", 9700)
    );

    public List<Song> searchSongs(String keyword) {
        String query = keyword == null ? "" : keyword.trim();
        List<Song> curatedSongs = SONG_LIBRARY.stream()
                .map(seed -> new RankedSeed(seed, score(seed, query)))
                .filter(item -> query.isEmpty() || item.score() > 0)
                .sorted(Comparator.comparingInt(RankedSeed::score).reversed())
                .limit(query.isEmpty() ? 16 : 8)
                .map(item -> createMockSong(SONG_LIBRARY.indexOf(item.seed())))
                .toList();

        List<Song> remoteSongs = searchNeteaseSongs(query);
        List<Song> mergedSongs = new ArrayList<>(curatedSongs);
        for (Song remoteSong : remoteSongs) {
            boolean exists = mergedSongs.stream().anyMatch(song ->
                    song.getSongName().equalsIgnoreCase(remoteSong.getSongName()) &&
                            song.getSinger().equalsIgnoreCase(remoteSong.getSinger()));
            if (!exists) {
                mergedSongs.add(remoteSong);
            }
            if (mergedSongs.size() >= 10) {
                break;
            }
        }

        if (!mergedSongs.isEmpty()) {
            return mergedSongs;
        }

        return SONG_LIBRARY.stream()
                .sorted(Comparator.comparingInt(SongSeed::popularity).reversed())
                .limit(8)
                .map(seed -> createMockSong(SONG_LIBRARY.indexOf(seed)))
                .toList();
    }

    public List<Song> getSongsByScene(String scene) {
        String query = scene == null ? "" : scene.trim();
        List<Song> result = new ArrayList<>();
        for (int i = 0; i < SONG_LIBRARY.size(); i++) {
            SongSeed seed = SONG_LIBRARY.get(i);
            if (seed.sceneTags().contains(query) || query.contains(seed.genre()) ||
                    containsAny(query, seed.sceneTags(), seed.keywords(), seed.singer())) {
                result.add(createMockSong(i));
            }
        }
        if (result.isEmpty()) {
            return searchSongs(query);
        }
        return result.stream()
                .sorted((a, b) -> b.getPopularity() - a.getPopularity())
                .limit(10)
                .toList();
    }

    public List<Song> getSongsByGenre(String genre) {
        return searchSongs(genre);
    }

    public Song getSongById(Long id) {
        int index = (int) ((id == null ? 1 : id) - 1);
        return createMockSong(Math.floorMod(index, SONG_LIBRARY.size()));
    }

    private int score(SongSeed seed, String query) {
        if (query.isEmpty()) {
            return seed.popularity();
        }

        int score = 0;
        if (query.contains(seed.songName())) score += 160;
        if (query.contains(seed.singer())) score += 140;
        if (query.contains(seed.genre())) score += 80;
        if (containsAny(query, seed.sceneTags(), seed.keywords(), seed.singer(), seed.songName())) score += 70;
        if (query.contains("慢") && containsAny("慢歌", seed.sceneTags(), seed.keywords())) score += 70;
        if ((query.contains("嗨") || query.contains("蹦迪")) && containsAny("朋友聚会 动感舞曲 嗨", seed.sceneTags(), seed.keywords())) score += 70;
        if ((query.contains("告白") || query.contains("情歌")) && containsAny("告白 情歌 浪漫约会", seed.sceneTags(), seed.keywords(), seed.genre())) score += 80;
        if (query.contains("粤语") && containsAny("粤语", seed.sceneTags(), seed.keywords(), seed.genre())) score += 110;
        if (query.contains("老歌") && containsAny("经典老歌 经典", seed.sceneTags(), seed.genre())) score += 80;
        if (query.contains("火") && containsAny("KTV热门 很火", seed.sceneTags(), seed.keywords())) score += 70;
        return score + seed.popularity() / 1000;
    }

    private boolean containsAny(String query, String... texts) {
        for (String text : texts) {
            if (text == null) continue;
            for (String token : query.split("[,，、\\s]+")) {
                if (!token.isEmpty() && text.contains(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Song createMockSong(int index) {
        SongSeed seed = SONG_LIBRARY.get(Math.floorMod(index, SONG_LIBRARY.size()));
        Song song = new Song();
        song.setId((long) index + 1);
        song.setSongName(seed.songName());
        song.setSinger(seed.singer());
        song.setAlbum("KTV AI 精选");
        song.setGenre(seed.genre());
        song.setSceneTags(seed.sceneTags());
        song.setExternalId("mock_" + (index + 1));
        song.setCoverUrl("https://picsum.photos/seed/ktv-ai-" + (index + 1) + "/300/300");
        hydrateCuratedSongFromNetease(song, seed);
        song.setPopularity(seed.popularity());
        song.setDuration(180 + (index % 9) * 18);
        return song;
    }

    private void hydrateCuratedSongFromNetease(Song song, SongSeed seed) {
        Song neteaseSong = findBestNeteaseSong(seed.songName() + " " + seed.singer(), seed.songName(), seed.singer());
        if (neteaseSong == null) {
            song.setAudioUrl(null);
            return;
        }

        song.setId(neteaseSong.getId());
        song.setExternalId(neteaseSong.getExternalId());
        song.setAudioUrl(neteaseSong.getAudioUrl());
        song.setCoverUrl(neteaseSong.getCoverUrl());
        song.setAlbum(neteaseSong.getAlbum());
        if (neteaseSong.getDuration() != null && neteaseSong.getDuration() > 0) {
            song.setDuration(neteaseSong.getDuration());
        }
    }

    private Song findBestNeteaseSong(String keyword, String expectedName, String expectedSinger) {
        String cacheKey = expectedName + "::" + expectedSinger;
        if (neteaseExactCache.containsKey(cacheKey)) {
            return cloneSong(neteaseExactCache.get(cacheKey));
        }

        Long curatedId = CURATED_NETEASE_IDS.get(cacheKey);
        if (curatedId != null) {
            Song curatedSong = buildNeteaseSongById(curatedId, expectedName, expectedSinger);
            if (curatedSong != null) {
                neteaseExactCache.put(cacheKey, cloneSong(curatedSong));
                return curatedSong;
            }
        }

        List<Song> songs = searchNeteaseSongs(keyword);
        Song bestSong = songs.stream()
                .filter(song -> isStrictSongMatch(song, expectedName, expectedSinger))
                .findFirst()
                .orElse(null);

        if (bestSong != null) {
            neteaseExactCache.put(cacheKey, cloneSong(bestSong));
            return cloneSong(bestSong);
        }
        return null;
    }

    private Song buildNeteaseSongById(Long neteaseId, String songName, String singer) {
        Song song = fetchNeteaseSongDetail(neteaseId);
        if (song == null || !isStrictSongMatch(song, songName, singer)) {
            log.warn("Netease curated id mismatch: id={}, expected={}::{}, actual={}::{}",
                    neteaseId,
                    songName,
                    singer,
                    song == null ? "null" : song.getSongName(),
                    song == null ? "null" : song.getSinger());
            return null;
        }
        return song;
    }

    private Song fetchNeteaseSongDetail(Long neteaseId) {
        String url = "https://music.163.com/api/song/detail/?ids=[" + neteaseId + "]";
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 KTV-AI-Agent-Assistant")
                .header("Referer", "https://music.163.com/")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }

            JsonNode songs = objectMapper.readTree(response.body().string()).path("songs");
            if (!songs.isArray() || songs.isEmpty()) {
                return null;
            }

            JsonNode item = songs.get(0);
            Long id = item.path("id").asLong();
            String name = item.path("name").asText("");
            String singer = readArtistName(item);
            if (id == 0 || name.isBlank() || singer.isBlank()) {
                return null;
            }

            Song song = new Song();
            song.setId(id);
            song.setSongName(name);
            song.setSinger(singer);
            song.setAlbum(item.path("album").path("name").asText("网易云音乐"));
            song.setExternalId("netease_" + id);
            song.setCoverUrl(item.path("album").path("picUrl").asText("https://picsum.photos/seed/netease-" + id + "/300/300"));
            song.setAudioUrl("https://music.163.com/song/media/outer/url?id=" + id + ".mp3");
            song.setDuration(Math.max(120, item.path("duration").asInt(240000) / 1000));
            return song;
        } catch (Exception e) {
            log.warn("Netease song detail failed: id={}, error={}", neteaseId, e.getMessage());
            return null;
        }
    }

    private boolean isStrictSongMatch(Song song, String expectedName, String expectedSinger) {
        if (song == null) {
            return false;
        }
        return normalizeText(song.getSongName()).equals(normalizeText(expectedName))
                && normalizeText(song.getSinger()).equals(normalizeText(expectedSinger));
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.replaceAll("\\s+", "").trim();
    }

    private Song cloneSong(Song source) {
        if (source == null) {
            return null;
        }
        Song song = new Song();
        song.setId(source.getId());
        song.setSongName(source.getSongName());
        song.setSinger(source.getSinger());
        song.setAlbum(source.getAlbum());
        song.setGenre(source.getGenre());
        song.setSceneTags(source.getSceneTags());
        song.setExternalId(source.getExternalId());
        song.setAudioUrl(source.getAudioUrl());
        song.setCoverUrl(source.getCoverUrl());
        song.setPopularity(source.getPopularity());
        song.setDuration(source.getDuration());
        return song;
    }

    private List<Song> searchNeteaseSongs(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = "https://music.163.com/api/search/get/web?s=" + encodedKeyword + "&type=1&limit=10&offset=0";
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 KTV-AI-Agent-Assistant")
                .header("Referer", "https://music.163.com/")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return List.of();
            }

            JsonNode songs = objectMapper.readTree(response.body().string()).path("result").path("songs");
            if (!songs.isArray() || songs.isEmpty()) {
                return List.of();
            }

            List<Song> result = new ArrayList<>();
            for (JsonNode item : songs) {
                Long neteaseId = item.path("id").asLong();
                String songName = item.path("name").asText("");
                String singer = readArtistName(item);
                if (neteaseId == 0 || songName.isBlank() || singer.isBlank()) {
                    continue;
                }

                Song song = new Song();
                song.setId(neteaseId);
                song.setSongName(songName);
                song.setSinger(singer);
                song.setAlbum(item.path("album").path("name").asText("网易云音乐"));
                song.setGenre(keyword);
                song.setSceneTags(keyword);
                song.setExternalId("netease_" + neteaseId);
                song.setCoverUrl(item.path("album").path("picUrl").asText("https://picsum.photos/seed/netease-" + neteaseId + "/300/300"));
                song.setAudioUrl("https://music.163.com/song/media/outer/url?id=" + neteaseId + ".mp3");
                song.setPopularity(7000 + Math.min(result.size() * 300, 2500));
                song.setDuration(Math.max(120, item.path("duration").asInt(240000) / 1000));
                result.add(song);
            }
            return result;
        } catch (Exception e) {
            log.warn("Netease music search failed, fallback to local library: {}", e.getMessage());
            return List.of();
        }
    }

    private String readArtistName(JsonNode item) {
        JsonNode artists = item.path("artists");
        if (artists.isArray() && !artists.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (JsonNode artist : artists) {
                String name = artist.path("name").asText("");
                if (!name.isBlank()) {
                    names.add(name);
                }
            }
            if (!names.isEmpty()) {
                return String.join("/", names);
            }
        }
        return "";
    }

    private record SongSeed(String songName, String singer, String genre, String sceneTags, String keywords, int popularity) {}

    private record RankedSeed(SongSeed seed, int score) {}
}
