package com.xk.player.ui;

import com.xk.player.tools.SongSeacher.SearchInfo;

public class MVSearchItem extends LTableItem {

	public MVSearchItem(SearchInfo info) {
		super(info);
	}

	@Override
	protected void download() {
		PlayUI ui = PlayUI.getInstance();
		ui.playMv(info.url);
	}

}
