import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 
 * @author Babak Hoseini
 * 
 * This class cloned from Github 
 * https://github.com/Freydoonk/PersianCalendar
 *
 */

public class PersianCalendar extends Calendar
{
	private static final int ONE_SECOND = 1000;
	private static final int ONE_MINUTE = 60 * ONE_SECOND;
	private static final int ONE_HOUR = 60 * ONE_MINUTE;

	private static short GREGORIAN_DAYS_IN_MONTH[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	private static short PERSIAN_DAYS_IN_MONTH[] = {31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29};

	static final int MIN_VALUES[] = {
			0,              // ERA
			1,              // YEAR
			1,              // MONTH
			1,              // WEEK_OF_YEAR
			0,              // WEEK_OF_MONTH
			1,              // DAY_OF_MONTH
			1,              // DAY_OF_YEARp
			1,              // DAY_OF_WEEKp
			1,              // DAY_OF_WEEK_IN_MONTH
			AM,             // AM_PM
			0,              // HOUR
			0,              // HOUR_OF_DAY
			0,              // MINUTE
			0,              // SECOND
			0,              // MILLISECOND
			-13 * ONE_HOUR, // ZONE_OFFSET (UNIX compatibility)
			0               // DST_OFFSET
	};
	static final int LEAST_MAX_VALUES[] = {
			1,              // ERA
			292269054,      // YEAR
			12,             // MONTH
			52,             // WEEK_OF_YEAR
			4,              // WEEK_OF_MONTH
			29,             // DAY_OF_MONTH
			365,            // DAY_OF_YEAR
			FRIDAY,         // DAY_OF_WEEK
			4,              // DAY_OF_WEEK_IN
			PM,             // AM_PM
			11,             // HOUR
			23,             // HOUR_OF_DAY
			59,             // MINUTE
			59,             // SECOND
			999,            // MILLISECOND
			14 * ONE_HOUR,  // ZONE_OFFSET
			20 * ONE_MINUTE // DST_OFFSET (historical least maximum)
	};
	static final int MAX_VALUES[] = {
			1,              // ERA
			292278994,      // YEAR
			12,             // MONTH
			53,             // WEEK_OF_YEAR
			6,              // WEEK_OF_MONTH
			31,             // DAY_OF_MONTH
			366,            // DAY_OF_YEAR
			FRIDAY,         // DAY_OF_WEEK
			6,              // DAY_OF_WEEK_IN
			PM,             // AM_PM
			11,             // HOUR
			23,             // HOUR_OF_DAY
			59,             // MINUTE
			59,             // SECOND
			999,            // MILLISECOND
			14 * ONE_HOUR,  // ZONE_OFFSET
			2 * ONE_HOUR    // DST_OFFSET (double summer time)
	};

	private TimeZone mTimeZone = TimeZone.getDefault();
	private boolean mIsTimeAdjusted = false;

	public PersianCalendar()
	{
		this(TimeZone.getDefault(), Locale.getDefault());
	}

	public PersianCalendar(Calendar calendar)
	{
		this(calendar.getTimeZone(), Locale.getDefault(), calendar);
	}

	public PersianCalendar(TimeZone zone, Locale aLocale)
	{
		this(zone, Locale.getDefault(), Calendar.getInstance(zone, aLocale));
	}

	public PersianCalendar(TimeZone zone, Locale aLocale, Calendar calendar)
	{
		super(zone, aLocale);
		mTimeZone = zone;

		DateStruct persianDate = gregorianToPersian(calendar.get(YEAR), calendar.get(MONTH), calendar.get(DATE));
		set(persianDate.getYear(), persianDate.getMonth(), persianDate.getDay());
		complete();
	}

	public PersianCalendar(int year, int month, int dayOfMonth)
	{
		this(year, month, dayOfMonth, 0, 0, 0, 0);
	}

	public PersianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second, int millis)
	{
		super();

		DateStruct dateStruct = persianToGregorian(fields[1], fields[2], fields[5]);
		GregorianCalendar gregorianCalendar = new GregorianCalendar(dateStruct.getYear(), dateStruct.getMonth(),
				dateStruct.getDay(), hourOfDay, minute, second);

		initializeCalendar(year, month, dayOfMonth, hourOfDay, minute, second, millis, gregorianCalendar);
	}

	public static DateStruct gregorianToPersian(int year, int month, int day)
	{
		if (month > 11 || month < -11)
		{
			throw new IllegalArgumentException();
		}
		int persianYear;
		int persianMonth;
		int persianDay;

		int gregorianDayNo, persianDayNo;
		int persianNP;
		int i;

		year = year - 1600;
		day = day - 1;

		gregorianDayNo = 365 * year
				+ (int) Math.floor((year + 3) / 4)
				- (int) Math.floor((year + 99) / 100)
				+ (int) Math.floor((year + 399) / 400);

		for (i = 0; i < month; ++i)
		{
			gregorianDayNo += GREGORIAN_DAYS_IN_MONTH[i];
		}

		if (month > 1 && ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)))
		{
			++gregorianDayNo;
		}

		gregorianDayNo += day;

		persianDayNo = gregorianDayNo - 79;

		persianNP = (int) Math.floor(persianDayNo / 12053);
		persianDayNo = persianDayNo % 12053;

		persianYear = 979 + 33 * persianNP + 4 * (persianDayNo / 1461);
		persianDayNo = persianDayNo % 1461;

		if (persianDayNo >= 366)
		{
			persianYear += (int) Math.floor((persianDayNo - 1) / 365);
			persianDayNo = (persianDayNo - 1) % 365;
		}

		for (i = 0; i < 11 && persianDayNo >= PERSIAN_DAYS_IN_MONTH[i]; ++i)
		{
			persianDayNo -= PERSIAN_DAYS_IN_MONTH[i];
		}
		persianMonth = i;
		persianDay = persianDayNo + 1;

		return new DateStruct(CalendarType.PERSIAN, persianYear, persianMonth, persianDay);
	}

	public static DateStruct persianToGregorian(int year, int month, int day)
	{
		if (month > 11 || month < -11)
			throw new IllegalArgumentException();

		int gregorianYear;
		int gregorianMonth;
		int gregorianDay;
		int gregorianDayNo;
		int persianDayNo;
		int leap;
		int i;

		year = year - 979;
		day = day - 1;

		persianDayNo = 365 * year
				+ (year / 33) * 8
				+ (int) Math.floor(((year % 33) + 3) / 4);

		for (i = 0; i < month; ++i)
		{
			persianDayNo += PERSIAN_DAYS_IN_MONTH[i];
		}

		persianDayNo += day;

		gregorianDayNo = persianDayNo + 79;

		gregorianYear = 1600 + 400 * (int) Math.floor(gregorianDayNo / 146097); /* 146097 = 365*400 + 400/4 - 400/100 + 400/400 */
		gregorianDayNo = gregorianDayNo % 146097;

		leap = 1;
		if (gregorianDayNo >= 36525) /* 36525 = 365*100 + 100/4 */
		{
			gregorianDayNo--;
			gregorianYear += 100 * (int) Math.floor(gregorianDayNo / 36524); /* 36524 = 365*100 + 100/4 - 100/100 */
			gregorianDayNo = gregorianDayNo % 36524;

			if (gregorianDayNo >= 365)
				gregorianDayNo++;
			else
				leap = 0;
		}

		gregorianYear += 4 * (int) Math.floor(gregorianDayNo / 1461); /* 1461 = 365*4 + 4/4 */
		gregorianDayNo = gregorianDayNo % 1461;

		if (gregorianDayNo >= 366)
		{
			leap = 0;

			gregorianDayNo--;
			gregorianYear += (int) Math.floor(gregorianDayNo / 365);
			gregorianDayNo = gregorianDayNo % 365;
		}

		for (i = 0; gregorianDayNo >= GREGORIAN_DAYS_IN_MONTH[i] + ((i == 1 && leap == 1) ? i : 0); i++)
		{
			gregorianDayNo -= GREGORIAN_DAYS_IN_MONTH[i] + ((i == 1 && leap == 1) ? i : 0);
		}
		gregorianMonth = i;
		gregorianDay = gregorianDayNo + 1;

		return new DateStruct(CalendarType.GREGORIAN, gregorianYear, gregorianMonth, gregorianDay);
	}

	public static int weekOfYear(int dayOfYear, int year)
	{
		switch (dayOfWeek(PersianCalendar.persianToGregorian(year, 0, 1)))
		{
			case 2:
				dayOfYear++;
				break;
			case 3:
				dayOfYear += 2;
				break;
			case 4:
				dayOfYear += 3;
				break;
			case 5:
				dayOfYear += 4;
				break;
			case 6:
				dayOfYear += 5;
				break;
			case 7:
				dayOfYear--;
				break;
		}

		dayOfYear = (int) Math.floor(dayOfYear / 7);
		return dayOfYear + 1;
	}

	public static int dayOfWeek(DateStruct dateStruct)
	{
		Calendar cal = new GregorianCalendar(dateStruct.getYear(), dateStruct.getMonth(), dateStruct.getDay());
		return cal.get(DAY_OF_WEEK);

	}

	private void initializeCalendar(int year, int month, int dayOfMonth, int hourOfDay,
	                                int minute, int second, int millis, GregorianCalendar gregorianCalendar)
	{
		super.set(YEAR, year);
		super.set(MONTH, month);
		super.set(DAY_OF_MONTH, dayOfMonth);

		if (hourOfDay >= 12 && hourOfDay <= 23)
		{
			super.set(AM_PM, PM);
			super.set(HOUR, hourOfDay - 12);
		}
		else
		{
			super.set(HOUR, hourOfDay);
			super.set(AM_PM, AM);
		}

		super.set(HOUR_OF_DAY, hourOfDay);
		super.set(MINUTE, minute);
		super.set(SECOND, second);
		super.set(MILLISECOND, millis);

		super.time = gregorianCalendar.getTimeInMillis();

		this.mIsTimeAdjusted = true;
	}

	public boolean isLeapYear(int year)
	{
		return (year % 33 == 1 || year % 33 == 5 || year % 33 == 9 || year % 33 == 13 ||
				year % 33 == 17 || year % 33 == 22 || year % 33 == 26 || year % 33 == 30);
	}

	public GregorianCalendar getGregorianCalendar()
	{
		DateStruct gregorianDate = persianToGregorian(internalGet(YEAR), internalGet(MONTH), internalGet(DAY_OF_MONTH));

		return new GregorianCalendar(gregorianDate.getYear(), gregorianDate.getMonth(),
				gregorianDate.getDay(), internalGet(HOUR_OF_DAY), internalGet(MINUTE), internalGet(SECOND));
	}

	public int getMaxDaysInMonth()
	{
		boolean leapYear = isLeapYear(fields[YEAR]);
		int month = fields[MONTH];

		if (leapYear && month == 11)
		{
			return PERSIAN_DAYS_IN_MONTH[month] + 1;
		}

		return PERSIAN_DAYS_IN_MONTH[month];
	}

	public int getDayOfWeek()
	{
		return this.get(Calendar.DAY_OF_WEEK) % 7;
	}

	public int getYear()
	{
		return this.get(Calendar.YEAR);
	}

	public int getMonth()
	{
		return this.get(Calendar.MONTH);
	}

	public int getDayOfMonth()
	{
		return this.get(Calendar.DATE);
	}

	public int getHour()
	{
		return this.get(Calendar.HOUR_OF_DAY);
	}

	public int getMinute()
	{
		return this.get(Calendar.MINUTE);
	}

	public DateStruct getDate()
	{
		return new DateStruct(CalendarType.PERSIAN, getYear(), getMonth(), getDayOfMonth());
	}

	public void addDay(int value)
	{
		this.add(Calendar.DATE, value);
	}

	public void addMonth(int value)
	{
		this.add(Calendar.MONTH, value);
	}

	@Override
	protected void computeTime()
	{
		if (!isTimeSet && !mIsTimeAdjusted)
		{
			Calendar cal = GregorianCalendar.getInstance(mTimeZone);
			if (!isSet(HOUR_OF_DAY))
			{
				super.set(HOUR_OF_DAY, cal.get(HOUR_OF_DAY));
			}
			if (!isSet(HOUR))
			{
				super.set(HOUR, cal.get(HOUR));
			}
			if (!isSet(MINUTE))
			{
				super.set(MINUTE, cal.get(MINUTE));
			}
			if (!isSet(SECOND))
			{
				super.set(SECOND, cal.get(SECOND));
			}
			if (!isSet(MILLISECOND))
			{
				super.set(MILLISECOND, cal.get(MILLISECOND));
			}
			if (!isSet(ZONE_OFFSET))
			{
				super.set(ZONE_OFFSET, cal.get(ZONE_OFFSET));
			}
			if (!isSet(DST_OFFSET))
			{
				super.set(DST_OFFSET, cal.get(DST_OFFSET));
			}
			if (!isSet(AM_PM))
			{
				super.set(AM_PM, cal.get(AM_PM));
			}

			if (internalGet(HOUR_OF_DAY) >= 12 && internalGet(HOUR_OF_DAY) <= 23)
			{
				super.set(AM_PM, PM);
				super.set(HOUR, internalGet(HOUR_OF_DAY) - 12);
			}
			else
			{
				super.set(HOUR, internalGet(HOUR_OF_DAY));
				super.set(AM_PM, AM);
			}

			DateStruct dateStruct = persianToGregorian(internalGet(YEAR), internalGet(MONTH), internalGet(DAY_OF_MONTH));
			cal.set(dateStruct.getYear(), dateStruct.getMonth(), dateStruct.getDay()
					, internalGet(HOUR_OF_DAY), internalGet(MINUTE), internalGet(SECOND));
			time = cal.getTimeInMillis();

		}
		else if (!isTimeSet)
		{
			if (internalGet(HOUR_OF_DAY) >= 12 && internalGet(HOUR_OF_DAY) <= 23)
			{
				super.set(AM_PM, PM);
				super.set(HOUR, internalGet(HOUR_OF_DAY) - 12);
			}
			else
			{
				super.set(HOUR, internalGet(HOUR_OF_DAY));
				super.set(AM_PM, AM);
			}

			super.set(ZONE_OFFSET, mTimeZone.getRawOffset());
			super.set(DST_OFFSET, mTimeZone.getDSTSavings());
			super.time = getGregorianCalendar().getTimeInMillis();
		}
	}

	@Override
	protected void computeFields()
	{
		boolean temp = isTimeSet;
		if (!areFieldsSet)
		{
			setMinimalDaysInFirstWeek(1);
			setFirstDayOfWeek(SATURDAY);

			//Day_Of_Year
			int dayOfYear = 0;
			int index = 0;

			while (index < fields[2])
			{
				dayOfYear += PERSIAN_DAYS_IN_MONTH[index++];
			}
			dayOfYear += fields[5];
			super.set(DAY_OF_YEAR, dayOfYear);
			//***

			//Day_of_Week
			super.set(DAY_OF_WEEK, dayOfWeek(persianToGregorian(fields[1], fields[2], fields[5])));
			//***

			//Day_Of_Week_In_Month
			if (0 < fields[5] && fields[5] < 8)
			{
				super.set(DAY_OF_WEEK_IN_MONTH, 1);
			}

			if (7 < fields[5] && fields[5] < 15)
			{
				super.set(DAY_OF_WEEK_IN_MONTH, 2);
			}

			if (14 < fields[5] && fields[5] < 22)
			{
				super.set(DAY_OF_WEEK_IN_MONTH, 3);
			}

			if (21 < fields[5] && fields[5] < 29)
			{
				super.set(DAY_OF_WEEK_IN_MONTH, 4);
			}

			if (28 < fields[5] && fields[5] < 32)
			{
				super.set(DAY_OF_WEEK_IN_MONTH, 5);
			}
			//***

			//Week_Of_Year
			super.set(WEEK_OF_YEAR, weekOfYear(fields[6], fields[1]));
			//***

			//Week_Of_Month
			super.set(WEEK_OF_MONTH, weekOfYear(fields[6], fields[1]) - weekOfYear(fields[6] - fields[5], fields[1]) + 1);
			//

			isTimeSet = temp;
		}
	}

	@Override
	public void add(int field, int amount)
	{
		if (field == YEAR)
		{
			super.set(YEAR, get(YEAR) + amount);
			if (get(DAY_OF_MONTH) == 30 && get(MONTH) == 11 && !isLeapYear(get(YEAR)))
			{
				super.set(DAY_OF_MONTH, 29);
			}

			complete();
		}
		else if (field == MONTH)
		{
			amount += get(MONTH);

			if (amount < 0)
			{
				add(YEAR, -1);
				super.set(MONTH, 0);
				add(MONTH, 12 + amount);
			}
			else
			{
				add(YEAR, amount / 12);
				super.set(MONTH, amount % 12);
				if (get(DAY_OF_MONTH) > PERSIAN_DAYS_IN_MONTH[amount % 12])
				{
					if (get(MONTH) == 11 && isLeapYear(get(YEAR)))
					{
						super.set(DAY_OF_MONTH, 30);
					}
					else
					{
						super.set(DAY_OF_MONTH, PERSIAN_DAYS_IN_MONTH[amount % 12]);
					}
				}
			}

			complete();
		}
		else if (field == DATE)
		{
			int actualMaxDay = getMaxDaysInMonth();
			amount += get(DATE);

			if (amount > actualMaxDay)
			{
				amount -= actualMaxDay;
				add(MONTH, 1);
				super.set(DATE, 0);
				add(DATE, amount);
			}
			else if (amount > 0)
			{
				super.set(DATE, amount);
			}
			else if (amount == 0)
			{
				add(MONTH, -1);
				super.set(DATE, getMaxDaysInMonth());
			}
			else if (amount <= 0)
			{
				add(MONTH, -1);
				super.set(DATE, 0);
				add(DATE, getMaxDaysInMonth() + amount);
			}

			complete();
		}
		else
		{
			DateStruct dateStruct = persianToGregorian(
					get(Calendar.YEAR),
					get(Calendar.MONTH),
					get(Calendar.DATE));

			GregorianCalendar gregorianCalendar =
					new GregorianCalendar(
							dateStruct.getYear(),
							dateStruct.getMonth(),
							dateStruct.getDay(),
							get(Calendar.HOUR_OF_DAY),
							get(Calendar.MINUTE),
							get(Calendar.SECOND)
					);

			gregorianCalendar.add(field, amount);

			dateStruct = gregorianToPersian(
					gregorianCalendar.get(Calendar.YEAR),
					gregorianCalendar.get(Calendar.MONTH),
					gregorianCalendar.get(Calendar.DATE)
			);

			initializeCalendar(
					dateStruct.getYear(),
					dateStruct.getMonth(),
					dateStruct.getDay(),
					gregorianCalendar.get(Calendar.HOUR_OF_DAY),
					gregorianCalendar.get(Calendar.MINUTE),
					gregorianCalendar.get(Calendar.SECOND),
					gregorianCalendar.get(Calendar.MILLISECOND),
					gregorianCalendar);

			complete();
		}
	}

	@Override
	public void roll(int field, boolean up)
	{
		roll(field, up ? +1 : -1);
	}

	@Override
	public void roll(int field, int amount)
	{
		if (amount == 0)
		{
			return;
		}

		if (field < 0 || field >= ZONE_OFFSET)
		{
			throw new IllegalArgumentException();
		}

		complete();

		switch (field)
		{
			case AM_PM:
			{
				if (amount % 2 != 0)
				{
					if (internalGet(AM_PM) == AM)
					{
						fields[AM_PM] = PM;
					}
					else
					{
						fields[AM_PM] = AM;
					}
					if (get(AM_PM) == AM)
					{
						super.set(HOUR_OF_DAY, get(HOUR));
					}
					else
					{
						super.set(HOUR_OF_DAY, get(HOUR) + 12);
					}
				}
				break;
			}
			case YEAR:
			{
				super.set(YEAR, internalGet(YEAR) + amount);
				if (internalGet(MONTH) == 11 && internalGet(DAY_OF_MONTH) == 30 && !isLeapYear(internalGet(YEAR)))
				{
					super.set(DAY_OF_MONTH, 29);
				}
				break;
			}
			case MINUTE:
			{
				int unit = 60;
				int m = (internalGet(MINUTE) + amount) % unit;
				if (m < 0)
				{
					m += unit;
				}
				super.set(MINUTE, m);
				break;
			}
			case SECOND:
			{
				int unit = 60;
				int s = (internalGet(SECOND) + amount) % unit;
				if (s < 0)
				{
					s += unit;
				}
				super.set(SECOND, s);
				break;
			}
			case MILLISECOND:
			{
				int unit = 1000;
				int ms = (internalGet(MILLISECOND) + amount) % unit;
				if (ms < 0)
				{
					ms += unit;
				}
				super.set(MILLISECOND, ms);
				break;
			}

			case HOUR:
			{
				super.set(HOUR, (internalGet(HOUR) + amount) % 12);
				if (internalGet(HOUR) < 0)
				{
					fields[HOUR] += 12;
				}
				if (internalGet(AM_PM) == AM)
				{
					super.set(HOUR_OF_DAY, internalGet(HOUR));
				}
				else
				{
					super.set(HOUR_OF_DAY, internalGet(HOUR) + 12);
				}

				break;
			}
			case HOUR_OF_DAY:
			{
				fields[HOUR_OF_DAY] = (internalGet(HOUR_OF_DAY) + amount) % 24;
				if (internalGet(HOUR_OF_DAY) < 0)
				{
					fields[HOUR_OF_DAY] += 24;
				}
				if (internalGet(HOUR_OF_DAY) < 12)
				{
					fields[AM_PM] = AM;
					fields[HOUR] = internalGet(HOUR_OF_DAY);
				}
				else
				{
					fields[AM_PM] = PM;
					fields[HOUR] = internalGet(HOUR_OF_DAY) - 12;
				}

			}
			case MONTH:
			{
				int mon = (internalGet(MONTH) + amount) % 12;
				if (mon < 0)
				{
					mon += 12;
				}
				super.set(MONTH, mon);

				int monthLen = PERSIAN_DAYS_IN_MONTH[mon];
				if (internalGet(MONTH) == 11 && isLeapYear(internalGet(YEAR)))
				{
					monthLen = 30;
				}
				if (internalGet(DAY_OF_MONTH) > monthLen)
				{
					super.set(DAY_OF_MONTH, monthLen);
				}
				break;
			}
			case DAY_OF_MONTH:
			{
				int unit = 0;
				if (0 <= get(MONTH) && get(MONTH) <= 5)
				{
					unit = 31;
				}
				if (6 <= get(MONTH) && get(MONTH) <= 10)
				{
					unit = 30;
				}
				if (get(MONTH) == 11)
				{
					if (isLeapYear(get(YEAR)))
					{
						unit = 30;
					}
					else
					{
						unit = 29;
					}
				}
				int d = (get(DAY_OF_MONTH) + amount) % unit;
				if (d < 0)
				{
					d += unit;
				}
				super.set(DAY_OF_MONTH, d);
				break;

			}
			case WEEK_OF_YEAR:
			{
				break;
			}
			case DAY_OF_YEAR:
			{
				int unit = (isLeapYear(internalGet(YEAR)) ? 366 : 365);
				int dayOfYear = (internalGet(DAY_OF_YEAR) + amount) % unit;
				dayOfYear = (dayOfYear > 0) ? dayOfYear : dayOfYear + unit;
				int month = 0, temp = 0;
				while (dayOfYear > temp)
				{
					temp += PERSIAN_DAYS_IN_MONTH[month++];
				}
				super.set(MONTH, --month);
				super.set(DAY_OF_MONTH, PERSIAN_DAYS_IN_MONTH[internalGet(MONTH)] - (temp - dayOfYear));
				break;
			}
			case DAY_OF_WEEK:
			{
				int index = amount % 7;
				if (index < 0)
				{
					index += 7;
				}
				int i = 0;
				while (i != index)
				{
					if (internalGet(DAY_OF_WEEK) == FRIDAY)
					{
						add(DAY_OF_MONTH, -6);
					}
					else
					{
						add(DAY_OF_MONTH, +1);
					}
					i++;
				}
				break;
			}

			default:
				throw new IllegalArgumentException();
		}

	}

	@Override
	public int getMinimum(int field)
	{
		return MIN_VALUES[field];
	}

	@Override
	public int getMaximum(int field)
	{
		return MAX_VALUES[field];
	}

	@Override
	public int getGreatestMinimum(int field)
	{
		return MIN_VALUES[field];
	}

	@Override
	public int getLeastMaximum(int field)
	{
		return LEAST_MAX_VALUES[field];
	}

	public static class DateStruct
	{
		private short mCalendarType;
		private int mYear;
		private int mMonth;
		private int mDay;

		public DateStruct(short calendarType, int year, int month, int day)
		{
			this.mCalendarType = calendarType;
			this.mYear = year;
			this.mMonth = month;
			this.mDay = day;
		}

		public short getCalendarType()
		{
			return mCalendarType;
		}

		public void setCalendarType(short calendarType)
		{
			mCalendarType = calendarType;
		}

		public int getYear()
		{
			return mYear;
		}

		public void setYear(int year)
		{
			mYear = year;
		}

		public int getMonth()
		{
			return mMonth;
		}

		public void setMonth(int month)
		{
			this.mMonth = month;
		}

		public int getDay()
		{
			return mDay;
		}

		public void setDay(int date)
		{
			mDay = date;
		}

		@Override
		public String toString()
		{
			return getYear() + "/" + getMonth() + "/" + getDay();
		}
	}

	public class CalendarType
	{
		public static final short GREGORIAN = 0;
		public static final short PERSIAN = 1;
	}
}