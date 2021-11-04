package carpettisaddition.commands.lifetime.removal;

import carpettisaddition.utils.DimensionWrapper;
import carpettisaddition.utils.Messenger;
import net.minecraft.text.BaseText;

import java.util.Objects;

public class TransDimensionRemovalReason extends RemovalReason
{
	private final DimensionWrapper newDimension;

	public TransDimensionRemovalReason(DimensionWrapper newDimension)
	{
		this.newDimension = Objects.requireNonNull(newDimension);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TransDimensionRemovalReason that = (TransDimensionRemovalReason) o;
		return Objects.equals(this.newDimension, that.newDimension);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.newDimension);
	}

	@Override
	public BaseText toText()
	{
		return Messenger.c(
				tr("trans_dimension"),
				"g  (",
				Messenger.formatting(tr("trans_dimension.to", Messenger.dimension(this.newDimension)), "g"),
				"g )"
		);
	}
}
