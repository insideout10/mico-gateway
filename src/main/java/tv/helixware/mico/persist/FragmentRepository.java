package tv.helixware.mico.persist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import tv.helixware.mico.model.Fragment;
import tv.helixware.mico.model.Part;

import java.util.List;
import java.util.stream.Stream;

/**
 * Provides persistency and REST access (via Spring Data REST) to Fragments.
 *
 * @since 0.1.0
 */
public interface FragmentRepository<T extends Fragment> extends PagingAndSortingRepository<T, Long> {

    @Query("select f from Fragment f where f.part.item.asset.guid = :guid")
    Page<T> findByAssetGUID(@Param("guid") String guid, Pageable page);

    @Query("select f from Fragment f where f.part = :part")
    List<T> findAllWherePartEquals(@Param("part") Part part);

}
