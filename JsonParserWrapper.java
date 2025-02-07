import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class JsonParserWrapper implements Closeable {
    private final JsonParser parser;

    public JsonParserWrapper(InputStream is) {
        this.parser = ConfiguredObjectMapper.createParser(is);
    }

    private static <T> Optional<Pair<T, List<T>>> headAndTail(List<T> l) {
        if (l.isEmpty()) {
            return Optional.empty();
        } else {
            final List<T> tail = l.stream().skip(1).collect(Collectors.toList());
            return Optional.of(Pair.of(l.get(0), tail));
        }
    }

    private JsonParserWrapper goToPath(List<String> path) {
        final Optional<Pair<String, List<String>>> headAndTail = headAndTail(path);
        return headAndTail.map(pair -> {
            try {
                final JsonToken firstToken = parser.nextToken();
                assert firstToken == JsonToken.START_OBJECT : "found " + firstToken;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            final String keyToFind = pair.getLeft();
            final List<String> remainingPath = pair.getRight();

            goToKey(keyToFind);
            return goToPath(remainingPath);

        }).orElse(this);
    }

    private void goToKey(String key) {
        try {
            while (true) {
                final JsonToken nextToken = parser.nextToken();
                switch (nextToken) {
                    case FIELD_NAME:
                        final String fieldName = parser.getCurrentName();
                        if (fieldName.equals(key)) {
                            return;
                        } else {
                            parser.skipChildren();
                        }
                        break;
                    case START_ARRAY:
                    case START_OBJECT:
                        parser.skipChildren();
                        break;
                    case END_OBJECT:
                        throw new IllegalArgumentException();
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T, R> R withJsonArrayAsStream(Class<T> clazz, Function<Stream<T>, R> callback) {
        try {
            final JsonToken nextToken = parser.nextToken();
            assert nextToken == JsonToken.START_ARRAY;
            final Iterator<T> iterator = new AbstractIterator<T>() {
                @Override
                protected T computeNext() {
                    try {
                        final JsonToken nextToken = parser.nextToken();
                        if (nextToken == JsonToken.END_ARRAY) {
                            return endOfData();
                        } else {
                            final T val = parser.readValueAs(clazz);
                            return Objects.requireNonNull(val);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            try (final Stream<T> stream = Streams.stream(iterator)) {
                final R result = callback.apply(stream);
                return result;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T, R> R withJsonArrayAsStream(List<String> path, Class<T> clazz, Function<Stream<T>, R> callback) {
        goToPath(path);
        return withJsonArrayAsStream(clazz, callback);
    }

    public static <T, R> R withJsonArrayAsStream(String json, List<String> path, Class<T> clazz, Function<Stream<T>, R> callback) {
        try(final InputStream is = IOUtils.toInputStream(json)) {
            return withJsonArrayAsStream(is, path, clazz, callback);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, R> R withJsonArrayAsStream(InputStream is, List<String> path, Class<T> clazz, Function<Stream<T>, R> callback) {
        try(final JsonParserWrapper jpw = new JsonParserWrapper(is)) {
            return jpw.withJsonArrayAsStream(path, clazz, callback);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        parser.close();
    }
}