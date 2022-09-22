package pdl.backend;

import java.util.Optional;
import java.util.List;

public interface Dao<T> {

  void create(final T t);

  Optional<T> retrieve(final long id);

  List<T> retrieveAll();

  List<T> retrieveGroup(final long idStart, final int size);

  List<T> retrieveWithFilters(final String type, final String nameImg);

  void update(final T t, final String[] params);

  void delete(final T t);
}
