package de.christinecoenen.code.programguide.plugins;


import de.christinecoenen.code.programguide.ProgramGuideRequest;

public interface IProgramGuideDownloader {
	void download(ProgramGuideRequest.Listener listener);
	void cancel();
}
