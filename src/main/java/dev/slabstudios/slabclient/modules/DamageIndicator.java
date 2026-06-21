package dev.slabstudios.slabclient.modules;

public class DamageIndicator {
	public double x, y, z;
	public double prevX, prevY, prevZ;
	public double vx, vy, vz;
	public float damage;
	public int age;
	public float roll;

	public DamageIndicator(double x, double y, double z, double dx, double dz, float damage) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.prevX = x;
		this.prevY = y;
		this.prevZ = z;
		this.damage = damage;
		this.age = 0;

		// Random roll angle between -15 and +15 degrees
		this.roll = (float) ((Math.random() - 0.5) * 30.0);

		// Trajectory carrying hit momentum away from the player
		this.vx = dx * 0.08 + (Math.random() - 0.5) * 0.04;
		this.vy = 0.15 + Math.random() * 0.06; // Jump upwards
		this.vz = dz * 0.08 + (Math.random() - 0.5) * 0.04;
	}
}
