package objects.util.graphics;

public abstract class ImageSource {
	public abstract void reshape(int width, int height);
	public abstract void render();
	public abstract void load(int[] pixels);
}
