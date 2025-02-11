/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

 *
 *
 */

package nl.digitalekabeltelevisie.data.mpeg.psi;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.LookUpList;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric
 *
 * based on ETSI TS 102 323 V1.5.1 (2012-01) Digital Video Broadcasting (DVB);
 * Carriage and signalling of TV-Anytime information
 * in DVB transport streams
 * 10.4 Related content table
 */
public class RCTsection extends TableSectionExtendedSyntax {

	private static final LookUpList link_type_list = new LookUpList.Builder().
			add(0x0,"Link information is a URI string only").
			add(0x1,"Link information is a binary locator only").
			add(0x2,"Link information is both a binary locator and a URI string").
			add(0x3,"Link information is through means of a descriptor").
			add(0x4 ,0xF,"DVB reserved").
			build();

	private static final LookUpList how_related_classification_scheme_id_list = new LookUpList.Builder().
			add(0x00,"urn:tva:metadata:HowRelatedCS:2004 [25], clause A.3.").
			add(0x01,"urn:tva:metadata:HowRelatedCS:2005 [26], clause A.3.").
			add(0x02,"urn:tva:metadata:HowRelatedCS:2007 [4], clause A.3.").
			add(0x03,0x2F,"DVB reserved").
			add(0x30,0x3F,"User Private").
			build();

	private final int year_offset;
	private final int link_count;
	private final int descriptor_loop_length;
	private final List<Descriptor>descriptor_loop;
	private final List<LinkInfo>	links = new ArrayList<>();

	public class LinkInfo implements TreeNode {

		public record PromotionalText(String iso639LanguageCode, DVBString promotional_text) implements TreeNode {
			@Override
			public KVP getJTreeNode(int modus) {
				final KVP t = new KVP("promotional_text");
				t.add(new KVP("ISO 639-2_language_code", iso639LanguageCode));
				t.add(new KVP("promotional_text",promotional_text));

				return t;
			}
		}

        private final int len;

		private final int link_type;
		private final int how_related_classification_scheme_id;

		private final int term_id;
		private final int group_id;
		private final int precedence;

		private int media_uri_length;
		private byte[] media_uri_byte;

		private final int number_items;
		private final List<PromotionalText> promotional_items = new ArrayList<>();

		private final int default_icon_flag;
		private final int icon_id;
		private final int descriptor_loop_length;
		private final List<Descriptor>descriptor_loop;

		protected LinkInfo(byte[] data, int offset, int len){
            this.len = len;
            final BitSource bs =new BitSource(data,offset,len);
			link_type = bs.readBits(4);
			bs.readBits(2); // reserved_future_use
			how_related_classification_scheme_id = bs.readBits(6);

			term_id  = bs.readBits(12);
			group_id  = bs.readBits(4);
			precedence = bs.readBits(4);

			if ((link_type == 0x00) || (link_type == 0x02)) {
				media_uri_length  = bs.readBits( 8);
				media_uri_byte = bs.readBytes(media_uri_length);
			}
			if((link_type == 0x01) ||(link_type == 0x02)) {
				throw new RuntimeException("Unimplemented dvb_binary_locator()");
			}
			bs.readBits(2); // reserved_future_use
			number_items = bs.readBits(6);
			for(int k = 0; k< number_items; k++){
				byte [] iso_639_2_language_code = bs.readBytes(3);
				//int promotional_text_length = bs.readBits(8);
				DVBString promotional_text_char = bs.readDVBString();
				PromotionalText pt = new PromotionalText(new String(iso_639_2_language_code),promotional_text_char);
				promotional_items.add(pt);
			}
			default_icon_flag  = bs.readBits(1);
			icon_id  = bs.readBits(3);
			descriptor_loop_length  = bs.readBits(12);
			byte[] descriptorData = bs.readBytes(descriptor_loop_length);
			descriptor_loop = DescriptorFactory.buildDescriptorList(descriptorData, 0, descriptor_loop_length, RCTsection.this);

		}

		@Override
		public KVP getJTreeNode(final int modus) {
			final KVP t = new KVP("link_info");
			t.add(new KVP("link_info_length", len));
			t.add(new KVP("link_type", link_type).setDescription(link_type_list.get(link_type)));
			t.add(new KVP("how_related_classification_scheme_id", how_related_classification_scheme_id)
					.setDescription(how_related_classification_scheme_id_list.get(how_related_classification_scheme_id)));
			t.add(new KVP("term_id", term_id));
			t.add(new KVP("group_id", group_id));
			t.add(new KVP("precedence", precedence));
			t.add(new KVP("media_uri_length", media_uri_length));
			t.add(new KVP("media_uri_byte", media_uri_byte));
			t.add(new KVP("number_items", number_items));
			Utils.addListJTree(t, promotional_items, modus, "Promotional text Items");
			t.add(new KVP("default_icon_flag", default_icon_flag));
			t.add(new KVP("icon_id", icon_id));
			t.add(new KVP("descriptor_loop_length", descriptor_loop_length));
			Utils.addListJTree(t, descriptor_loop, modus, "descriptor_loop");

			return t;
		}
	}

	public RCTsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data, parent);

		byte[] b = raw_data.getData();

		year_offset = Utils.getInt(b, 8, 2, Utils.MASK_16BITS);
		link_count = Utils.getInt(b, 10, 1, Utils.MASK_8BITS);
		int localOffset = 11;
		for (int i = 0; i < link_count; i++) {
			int link_info_length = Utils.getInt(b, localOffset, 2, Utils.MASK_12BITS);
			LinkInfo linkInfo = new LinkInfo(b, localOffset+2, link_info_length);
			links.add(linkInfo);
			localOffset +=2+link_info_length;
		}

		descriptor_loop_length = Utils.getInt(b, localOffset, 2, Utils.MASK_12BITS);
		localOffset += 2;

		descriptor_loop = DescriptorFactory.buildDescriptorList(b, localOffset,
				descriptor_loop_length, this);
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("AITsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=")
		.append(getTableType(tableId)).append(", ");

		return b.toString();
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new KVP("year_offset", year_offset));
		t.add(new KVP("link_count", link_count));
		Utils.addListJTree(t,links,modus,"link_infos");
		t.add(new KVP("descriptor_loop_length", descriptor_loop_length));
		Utils.addListJTree(t,descriptor_loop,modus,"descriptor_loop");
		return t;
	}
	
	@Override
	protected String getTableIdExtensionLabel() {
		return "service_id";
	}


}
