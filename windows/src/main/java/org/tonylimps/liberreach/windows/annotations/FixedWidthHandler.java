package org.tonylimps.liberreach.windows.annotations;

import javafx.scene.control.SplitPane;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class FixedWidthHandler {

	public void handle(Object... controllers){
		Arrays.stream(controllers).forEach(controller -> {
			Class<?> controllerClass = controller.getClass();
			Arrays.stream(controllerClass.getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(FixedWidth.class) && SplitPane.class.isAssignableFrom(field.getType()))
				.forEach(field -> {
					try{
						field.setAccessible(true);
						SplitPane pane = (SplitPane)field.get(controller);
						FixedWidth annotation = field.getAnnotation(FixedWidth.class);
						fixWidth(annotation.fixedIndex(), pane);
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
		});
	}

	private void fixWidth(int index, SplitPane pane) {
		AtomicReference<Double> fixedWidth = new AtomicReference<>();
		AtomicBoolean ignoreWidthChange = new AtomicBoolean(false);
		if(index == 0){
			fixedWidth.set(pane.getDividerPositions()[0] * pane.getWidth());
			pane.getDividers().get(0).positionProperty().addListener((obs, oldVal, newVal) -> {
				if (!ignoreWidthChange.get()) {
					fixedWidth.set(newVal.doubleValue() * pane.getWidth());
				}
			});

			pane.widthProperty().addListener((obs, oldVal, newVal) -> {
				if (newVal.doubleValue() != oldVal.doubleValue()) {
					ignoreWidthChange.set(true);
					double newPosition = fixedWidth.get() / newVal.doubleValue();
					pane.setDividerPosition(0, newPosition);
					ignoreWidthChange.set(false);
				}
			});
		}
		else if(index == 1){
			fixedWidth.set((1 - pane.getDividerPositions()[0]) * pane.getWidth());
			pane.getDividers().get(0).positionProperty().addListener((obs, oldVal, newVal) -> {
				if (!ignoreWidthChange.get()) {
					fixedWidth.set((1 - newVal.doubleValue()) * pane.getWidth());
				}
			});

			pane.widthProperty().addListener((obs, oldVal, newVal) -> {
				if (newVal.doubleValue() != oldVal.doubleValue()) {
					ignoreWidthChange.set(true);
					double newPosition = fixedWidth.get() / newVal.doubleValue();
					pane.setDividerPosition(0, 1 - newPosition);
					ignoreWidthChange.set(false);
				}
			});
		}
	}
}
