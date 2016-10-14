package net.byyd.archive.transform;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import net.byyd.archive.transform.util.TransformUtil;

public class OneItemLineFileSink<A> implements EventFeed<A> {

	private Transformer<A, String> tr;

	private int infoOnItemCount;
	private int renewOnItemCount;
	private long itemsProcessed;
	private PrintWriter pw;
	private OutputStream out;

	private UpdateStream update;
	private ItemsInfo info;

	public OneItemLineFileSink(Transformer<A, String> tr, OutputStream os) {
		this.tr = tr;
		out = os;
		pw = new PrintWriter(new OutputStreamWriter(os));
	}
	
	public OneItemLineFileSink(OutputStream os) {
		this(null, os);
	}

	public interface UpdateStream {
		OutputStream update(OutputStream current, long itemsProcessed);
	}

	public interface ItemsInfo {
		void info(long itemsProcessed, boolean finished);
	}

	@Override
	public void onEvent(A model) {
		if (tr != null) {
			pw.println(TransformUtil.oneLineJson(tr.transform(model)));
		} else {
			pw.println(model.toString());
		}
		pw.flush();
		itemsProcessed++;

		if (renewOnItemCount > 0 && itemsProcessed % renewOnItemCount == 0
				&& update != null) {
			pw.flush();
			pw.close();
			OutputStream os = update.update(out, itemsProcessed);
			if (os != null) {
				out = os;
			}
			pw = new PrintWriter(new OutputStreamWriter(out));
		}

		if (infoOnItemCount > 0 && itemsProcessed % infoOnItemCount == 0
				&& info != null) {
			info.info(itemsProcessed, false);
		}
	}
	
	@Override
	public void finish() {
		pw.close();
		if (infoOnItemCount > 0 && info != null) {
			info.info(itemsProcessed, true);
		}		
	}

	public int getRenewOnItemCount() {
		return renewOnItemCount;
	}

	public void setRenewOnItemCount(int renewOnItemCount) {
		this.renewOnItemCount = renewOnItemCount;
	}

	public UpdateStream getUpdate() {
		return update;
	}

	public void setUpdate(UpdateStream update) {
		this.update = update;
	}

	public ItemsInfo getInfo() {
		return info;
	}

	public void setInfo(ItemsInfo info) {
		this.info = info;
	}

	public long getItemsProcessed() {
		return itemsProcessed;
	}

	public int getInfoOnItemCount() {
		return infoOnItemCount;
	}

	public void setInfoOnItemCount(int infoOnItemCount) {
		this.infoOnItemCount = infoOnItemCount;
	}
	
}
