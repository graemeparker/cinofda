package net.byyd.archive.mapping;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.Impression;

public interface DomainModelMapper<ArchiveModel> {

	ArchiveModel map(AdEvent event);

	ArchiveModel map(Impression event);
}
