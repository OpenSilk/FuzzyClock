package hugo.weaving;
/**
 * Stub for tapas builds. Which /should/ cause the
 * compiler to just ignore the @DebugLog annotations.
 */
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Retention(SOURCE)
public @interface DebugLog {

}
