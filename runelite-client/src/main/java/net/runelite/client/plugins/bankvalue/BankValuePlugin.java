/*
 * Copyright (c) 2018, TheLonelyDev <https://github.com/TheLonelyDev>
 * Copyright (c) 2018, Jeremy Plsek <https://github.com/jplsek>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.bankvalue;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.awt.event.KeyEvent;

@PluginDescriptor(
	name = "Bank Value",
	description = "Show the value of your bank and/or current tab",
	tags = {"grand", "exchange", "high", "alchemy", "prices"}
)
public class BankValuePlugin extends Plugin implements KeyListener
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private BankCalculation bankCalculation;

	@Inject
	private BankTitle bankTitle;

	@Provides
	BankValueConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankValueConfig.class);
	}

	private boolean forceRefresh;

	@Override
	protected void shutDown()
	{
		clientThread.invokeLater(bankTitle::reset);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		Widget widgetBankTitleBar = client.getWidget(WidgetInfo.BANK_TITLE_BAR);

		if (widgetBankTitleBar == null || widgetBankTitleBar.isHidden())
		{
			return;
		}

		if (bankTitle.save(forceRefresh && chatboxFocused()))
		{
            forceRefresh = false;
			bankCalculation.calculate();
			bankTitle.update(bankCalculation.getGePrice(), bankCalculation.getHaPrice());
		}
	}

    boolean chatboxFocused()
    {
        Widget chatboxParent = client.getWidget(WidgetInfo.CHATBOX_INPUT);
        if (chatboxParent == null || chatboxParent.getOnKeyListener() == null)
        {
            return false;
        }

        return true;
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyPressed(KeyEvent e)
    {

    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        forceRefresh = true;
        /* Remove inputs if we're typing
        if (plugin.chatboxFocused())
        {
            Integer m = modified.get(e.getKeyCode());
            if (m != null)
            {
                modified.remove(e.getKeyCode());
            }
        }
        else
        {
            if (config.up().matches(e))
            {
                e.setKeyCode(KeyEvent.VK_UP);
            }
            else if (config.down().matches(e))
            {
                e.setKeyCode(KeyEvent.VK_DOWN);
            }
            else if (config.left().matches(e))
            {
                e.setKeyCode(KeyEvent.VK_LEFT);
            }
            else if (config.right().matches(e))
            {
                e.setKeyCode(KeyEvent.VK_RIGHT);
            }
        }*/
    }
}
