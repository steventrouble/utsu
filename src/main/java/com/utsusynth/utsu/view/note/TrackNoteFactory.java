package com.utsusynth.utsu.view.note;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.utsusynth.utsu.common.PitchUtils;
import com.utsusynth.utsu.common.quantize.QuantizedAddRequest;
import com.utsusynth.utsu.common.quantize.QuantizedEnvelope;
import com.utsusynth.utsu.common.quantize.QuantizedNote;
import com.utsusynth.utsu.common.quantize.Quantizer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Rectangle;

public class TrackNoteFactory {
	private static final int ROW_HEIGHT = 20;
	private static final int COL_WIDTH = 96;

	private final Quantizer quantizer;
	private final Provider<TrackLyric> lyricProvider;

	@Inject
	public TrackNoteFactory(Quantizer quantizer, Provider<TrackLyric> lyricProvider) {
		this.quantizer = quantizer;
		this.lyricProvider = lyricProvider;
	}

	public TrackNote createNote(QuantizedAddRequest request, TrackNoteCallback callback) {
		QuantizedNote qNote = request.getNote();
		int absStart = qNote.getStart() * (COL_WIDTH / qNote.getQuantization());
		int startCol = absStart / COL_WIDTH;
		int startMargin = absStart % COL_WIDTH;
		int absDuration = qNote.getDuration() * (COL_WIDTH / qNote.getQuantization());
		Rectangle note = new Rectangle();
		note.setWidth(absDuration - 1);
		note.setHeight(ROW_HEIGHT - 1);
		note.getStyleClass().addAll("track-note", "valid-note", "not-highlighted");
		StackPane.setMargin(note, new Insets(0, 0, 0, startMargin));

		Rectangle noteEdge = new Rectangle();
		StackPane.setAlignment(noteEdge, Pos.CENTER_RIGHT);
		noteEdge.setWidth(2);
		noteEdge.setHeight(note.getHeight());
		noteEdge.getStyleClass().add("drag-edge");

		Rectangle overlap = new Rectangle();
		StackPane.setAlignment(overlap, Pos.CENTER_RIGHT);
		overlap.setWidth(0);
		overlap.setHeight(note.getHeight());
		overlap.getStyleClass().add("drag-edge");

		StackPane layout = new StackPane();
		layout.setPickOnBounds(false);
		layout.setAlignment(Pos.CENTER_LEFT);
		GridPane.setRowIndex(layout, PitchUtils.pitchToRowNum(request.getPitch()));
		GridPane.setColumnIndex(layout, startCol);

		TrackLyric lyric = lyricProvider.get();
		lyric.setLeftMargin(startMargin);

		TrackNote trackNote =
				new TrackNote(note, noteEdge, overlap, lyric, layout, callback, quantizer);
		lyric.setVisibleLyric(request.getLyric());
		lyric.setVisibleAlias(request.getTrueLyric());

		return trackNote;
	}

	public TrackNote createDefaultNote(int row, int column, TrackNoteCallback callback) {
		Rectangle defaultNote = new Rectangle();
		defaultNote.setWidth(COL_WIDTH - 1);
		defaultNote.setHeight(ROW_HEIGHT - 1);
		defaultNote.getStyleClass().addAll("track-note", "invalid-note", "not-highlighted");

		Rectangle noteEdge = new Rectangle();
		StackPane.setAlignment(noteEdge, Pos.CENTER_RIGHT);
		noteEdge.setWidth(2);
		noteEdge.setHeight(defaultNote.getHeight());
		noteEdge.getStyleClass().add("drag-edge");

		Rectangle overlap = new Rectangle();
		StackPane.setAlignment(overlap, Pos.CENTER_RIGHT);
		overlap.setWidth(0);
		overlap.setHeight(defaultNote.getHeight());
		overlap.getStyleClass().add("drag-edge");

		StackPane layout = new StackPane();
		layout.setPickOnBounds(false);
		layout.setAlignment(Pos.CENTER_LEFT);
		GridPane.setRowIndex(layout, row);
		GridPane.setColumnIndex(layout, column);

		TrackLyric lyric = lyricProvider.get();

		TrackNote trackNote =
				new TrackNote(defaultNote, noteEdge, overlap, lyric, layout, callback, quantizer);
		lyric.setSongLyric(lyric.getLyric());

		return trackNote;
	}

	public TrackEnvelope createEnvelope(
			TrackNote note,
			QuantizedEnvelope qEnvelope,
			TrackEnvelopeCallback callback) {
		QuantizedNote qNote = note.getQuantizedNote();
		int noteQuantSize = COL_WIDTH / qNote.getQuantization();
		int startPos = qNote.getStart() * noteQuantSize;
		int endPos = startPos + (qNote.getDuration() * noteQuantSize);

		int envQuantSize = COL_WIDTH / QuantizedEnvelope.QUANTIZATION;
		int p1 = qEnvelope.getWidth(0) * envQuantSize;
		int p2 = qEnvelope.getWidth(1) * envQuantSize;
		int p3 = qEnvelope.getWidth(2) * envQuantSize;
		int p4 = qEnvelope.getWidth(3) * envQuantSize;
		int p5 = qEnvelope.getWidth(4) * envQuantSize;

		double v1 = 100 - (qEnvelope.getHeight(0) / 2.0);
		double v2 = 100 - (qEnvelope.getHeight(1) / 2.0);
		double v3 = 100 - (qEnvelope.getHeight(2) / 2.0);
		double v4 = 100 - (qEnvelope.getHeight(3) / 2.0);
		double v5 = 100 - (qEnvelope.getHeight(4) / 2.0);

		return new TrackEnvelope(
				new MoveTo(startPos, 100),
				new LineTo(startPos + p1, v1),
				new LineTo(startPos + p1 + p2, v2),
				new LineTo(startPos + p1 + p2 + p5, v5),
				new LineTo(endPos - p4 - p3, v3),
				new LineTo(endPos - p4, v4),
				new LineTo(endPos, 100),
				callback);
	}
}