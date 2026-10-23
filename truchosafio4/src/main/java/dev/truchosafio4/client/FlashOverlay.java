package dev.truchosafio4.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/** Dibuja la pantalla blanca del flash y la va desvaneciendo. */
public final class FlashOverlay {
	private static long startMs = 0;
	private static long durationMs = 0;

	private FlashOverlay() {
	}

	public static void trigger(int durationTicks) {
		startMs = System.currentTimeMillis();
		durationMs = durationTicks * 50L;
	}

	public static void render(DrawContext context, RenderTickCounter tickCounter) {
		if (durationMs <= 0) {
			return;
		}
		long elapsed = System.currentTimeMillis() - startMs;
		if (elapsed >= durationMs) {
			durationMs = 0;
			return;
		}

		// 30% del tiempo totalmente blanco, luego se desvanece
		float t = elapsed / (float) durationMs;
		float alpha = t < 0.3f ? 1.0f : 1.0f - (t - 0.3f) / 0.7f;
		int a = Math.max(0, Math.min(255, (int) (alpha * 255)));

		int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
		int height = MinecraftClient.getInstance().getWindow().getScaledHeight();
		context.fill(0, 0, width, height, (a << 24) | 0xFFFFFF);
	}
}
