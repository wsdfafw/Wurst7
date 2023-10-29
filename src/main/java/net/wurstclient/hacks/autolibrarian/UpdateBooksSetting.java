/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks.autolibrarian;

import net.wurstclient.settings.BookOffersSetting;
import net.wurstclient.settings.EnumSetting;

public final class UpdateBooksSetting
	extends EnumSetting<UpdateBooksSetting.UpdateBooks>
{
	public UpdateBooksSetting()
	{
		super("更新书籍",
			"当村民学会销售其中一本书时，自动更新所需书籍清单.\n\n"
				+ "\u00a7l关闭\u00a7r - 不要更新清单.\n\n"
				+ "\u00a7l移除\u00a7r - 从清单中移除该书，以便下一个村民学习不同的书.\n\n"
				+ "\u00a7l价格\u00a7r - 更新该书的最高价格，以便下一个村民不得不以更便宜的价格出售它.",
			UpdateBooks.values(), UpdateBooks.REMOVE);
	}
	
	public enum UpdateBooks
	{
		OFF("Off"),
		REMOVE("Remove"),
		PRICE("Price");
		
		private String name;
		
		private UpdateBooks(String name)
		{
			this.name = name;
		}
		
		public void update(BookOffersSetting wantedBooks, BookOffer offer)
		{
			int index = wantedBooks.indexOf(offer);
			
			switch(this)
			{
				case OFF:
				return;
				
				case REMOVE:
				wantedBooks.remove(index);
				break;
				
				case PRICE:
				if(offer.price() <= 1)
					wantedBooks.remove(index);
				else
					wantedBooks.replace(index, new BookOffer(offer.id(),
						offer.level(), offer.price() - 1));
				break;
			}
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
