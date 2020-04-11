package sdk.chat.core.avatar.gravatar;

/**
 * Easy Gravatar query building.
 * <p/>
 * Use {@link #init()} for the global singleton instance or construct your
 * own instance with {@link Builder}.
 */
public class Gravatar {
  public static final int MIN_IMAGE_SIZE_PIXEL = 1;
  public static final int MAX_IMAGE_SIZE_PIXEL = 2048;
  private static Gravatar singleton = null;
  final boolean ssl;
  final boolean extension;

  private Gravatar(boolean ssl, boolean extension) {
    this.ssl = ssl;
    this.extension = extension;
  }

  /**
   * The global default {@link Gravatar} instance.
   * <p/>
   * This instance is automatically initialized with defaults that are suitable to most
   * implementations.
   * <ul>
   * <li>SSL turned off by default</li>
   * <li>Image extension (.jpg) not displayed.</li>
   * </ul>
   * <p/>
   * If these settings do not meet the requirements of your application you can construct your own
   * instance with full control over the configuration by using {@link Gravatar.Builder}.
   */
  public static Gravatar init() {
    if (singleton == null) {
      singleton = new Builder().build();
    }
    return singleton;
  }

  /**
   * Start a Gravatar URL building request using the specified email address.
   */
  public RequestBuilder with(String email) {
    return new RequestBuilder(this, email);
  }

  /**
   * Fluent API for creating {@link Gravatar} instances.
   */
  @SuppressWarnings("UnusedDeclaration") // Public API.
  public static class Builder {
    private boolean ssl = false;
    private boolean extension = false;

    public Builder() {
    }

    /**
     * Specify that the secure Gravatar endpoint should be used by default.
     */
    public Builder ssl() {
      this.ssl = true;
      return this;
    }

    /**
     * Specify that file extension (.jpg) will be displayed at the end of the generated URL.
     */
    public Builder fileExtension() {
      this.extension = true;
      return this;
    }

    /**
     * Create the {@link Gravatar} instance.
     */
    public Gravatar build() {
      return new Gravatar(ssl, extension);
    }
  }

  public static class DefaultImage {
    public static final int MYSTERY_MAN = 0;
    public static final int IDENTICON = 1;
    public static final int MONSTER = 2;
    public static final int WAVATAR = 3;
    public static final int RETRO = 4;
    public static final int BLANK = 5;
  }

  public static class Rating {
    public static final int g = 0;
    public static final int pg = 1;
    public static final int r = 2;
    public static final int x = 3;
  }
}
