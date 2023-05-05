var JavaPackages = new JavaImporter(
Packages.org.joml.Matrix4f,
Packages.java.util.Random,
Packages.tage.GameObject
);
with(JavaPackages) {
object.setLocalTranslation((new Matrix4f()).translation((rand.nextInt(100) + (-50)), 50.0, (rand.nextInt(100) + (-50))));
}
