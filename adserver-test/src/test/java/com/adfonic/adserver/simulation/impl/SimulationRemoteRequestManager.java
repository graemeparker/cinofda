package com.adfonic.adserver.simulation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.adfonic.adserver.MutableWeightedCreative;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.filterout.RemoteRequestManager;
import com.adfonic.util.FastLinkedList;

public class SimulationRemoteRequestManager implements RemoteRequestManager {

	@Override
	public Future<List<MutableWeightedCreative>> filterOutCreatives(
			List<MutableWeightedCreative> creatives, TargetingContext context) {
		FutureTask<List<MutableWeightedCreative>> futureTask = new FutureTask<List<MutableWeightedCreative>>(new Runnable() {
					@Override
					public void run() {
					}
				}, creatives);
		futureTask.run();
		return futureTask;
	}

	@Override
	public List<MutableWeightedCreative> getThirdPartyCreatives(
			List<MutableWeightedCreative> creatives,
			FastLinkedList<MutableWeightedCreative> reusablePool) {
		List<MutableWeightedCreative> retval = new ArrayList<>();
		retval.addAll(creatives);

		return retval;
	}

	@Override
	public int getMaxWaiting() {
		return 60000;
	}

}
