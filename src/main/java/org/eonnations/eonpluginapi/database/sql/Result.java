package org.eonnations.eonpluginapi.database.sql;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@FunctionalInterface
public interface Result<T> {

    Stream<T> results();

    static <T> Result<T> makeResultsFromSet(ResultSet set, Class<T> resultClass) throws SQLException, ReflectiveOperationException {
        List<T> list = new ArrayList<>();
        while (set.next()) {
            Field[] fields = resultClass.getFields();
            T result = resultClass.getConstructor().newInstance();
            for (Field field : fields) {
                field.setAccessible(true);
                Object sqlResult = set.getObject(field.getName().toLowerCase());
                field.set(result, sqlResult);
            }
            list.add(result);
        }
        return list::stream;
    }

    static <T> Result<T> empty() {
        return Stream::empty;
    }
}
