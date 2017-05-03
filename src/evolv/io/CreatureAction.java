package evolv.io;

public interface CreatureAction {

	public void doAction(CreatureOld creatureOld, double modifier, double timeStep);

	public class Accelerate implements CreatureAction {

		@Override
		public void doAction(CreatureOld creatureOld, double amount, double timeStep) {
			creatureOld.accelerate(amount, timeStep);
		}
	}

	public class AdjustHue implements CreatureAction {

		@Override
		public void doAction(CreatureOld creatureOld, double modifier, double timeStep) {
			creatureOld.setHue(Math.abs(modifier) % 1.0f);
		}
	}

	public class AdjustMouthHue implements CreatureAction {

		@Override
		public void doAction(CreatureOld creatureOld, double modifier, double timeStep) {
			creatureOld.setMouthHue(Math.abs(modifier) % 1.0f);
		}

	}

	public class Eat implements CreatureAction {

		@Override
		public void doAction(CreatureOld creatureOld, double attemptedAmount, double timeStep) {
			creatureOld.eat(attemptedAmount, timeStep);
		}
	}

	public class Fight implements CreatureAction {

		@Override
		public void doAction(CreatureOld creatureOld, double amount, double timeStep) {
			creatureOld.fight(amount, timeStep);
		}
	}

	public class None implements CreatureAction {

		@Override
		public void doAction(CreatureOld creatureOld, double modifier, double timeStep) {
		}
	}

	public class Reproduce implements CreatureAction {

		@Override
		public void doAction(CreatureOld creatureOld, double modifier, double timeStep) {
			if (modifier <= 0) {
				return; // This creatureOld doesn't want to reproduce
			}
			if (creatureOld.getAge() < Configuration.MATURE_AGE) {
				return; // This creatureOld is too young
			}
			if (creatureOld.getEnergy() <= Configuration.SAFE_SIZE) {
				return; // This creatureOld is too small
			}

			double babySize = Configuration.SAFE_SIZE;
			creatureOld.reproduce(babySize, timeStep);
		}
	}

	public class Rotate implements CreatureAction {

		@Override
		public void doAction(CreatureOld creatureOld, double amount, double timeStep) {
			creatureOld.rotate(amount, timeStep);
		}
	}
}
