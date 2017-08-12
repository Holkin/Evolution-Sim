package evolv.io;

import evolv.io.temp.ICreature;

import java.util.Comparator;

import javax.swing.SortOrder;

public class CreatureComparators {
	private static abstract class BaseComparator implements Comparator<ICreature> {
		private final SortOrder sortOrder;

		public BaseComparator(SortOrder sortOrder) {
			this.sortOrder = sortOrder;
		}

		@Override
		public final int compare(ICreature creatureOld1, ICreature creatureOld2) {
			if (sortOrder == SortOrder.UNSORTED) {
				return 0;
			}
			int comparison = getComparison(creatureOld1, creatureOld2);
			return (sortOrder == SortOrder.ASCENDING) ? comparison : -comparison;
		}

		protected abstract int getComparison(ICreature creatureOld1, ICreature creatureOld2);
	}

	public static class NameComparator extends BaseComparator {
		public NameComparator(SortOrder sortOrder) {
			super(sortOrder);
		}

		@Override
		public int getComparison(ICreature creatureOld1, ICreature creatureOld2) {
			return creatureOld2.getName().compareTo(creatureOld1.getName());
		}
	}

	public static class SizeComparator extends BaseComparator {
		public SizeComparator(SortOrder sortOrder) {
			super(sortOrder);
		}

		@Override
		public int getComparison(ICreature creatureOld1, ICreature creatureOld2) {
			return Double.compare(creatureOld1.getEnergy(), creatureOld2.getEnergy());
		}
	}

	public static class AgeComparator extends BaseComparator {
		public AgeComparator(SortOrder sortOrder) {
			super(sortOrder);
		}

		@Override
		public int getComparison(ICreature creatureOld1, ICreature creatureOld2) {
			return (int) Math.signum(creatureOld1.getBirthTime() - creatureOld2.getBirthTime());
		}
	}

	public static class GenComparator extends BaseComparator {
		public GenComparator(SortOrder sortOrder) {
			super(sortOrder);
		}

		@Override
		public int getComparison(ICreature creatureOld1, ICreature creatureOld2) {
			return creatureOld1.getGen() - creatureOld2.getGen();
		}
	}
}
