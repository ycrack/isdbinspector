/**
 *  
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl) 
 * 
 *  This file is part of DVB Inspector.
 *  
 *  DVB Inspector is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  DVB Inspector is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with DVB Inspector.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  The author requests that he be notified of any application, applet, or 
 *  other binary that makes use of this code, but that's more out of curiosity 
 *  than anything and is not required.  
 * 
 */

package nl.digitalekabeltelevisie.data.mpeg;

public final class MPEGConstants {
	/**
	 * 
	 */
	private MPEGConstants() {
		// no constructor
	}
	public final static byte sync_byte=0x47;
	public final static int	PAYLOAD_PACKET_LENGTH=188;

	public final static int AVCHD_PACKET_LENGTH = 192;

	public final static int system_clock_frequency=27000000;
	
	public final static int NO_PCR_PID=0x1FFF;
	public static final int MAX_PIDS = 8192;
}
