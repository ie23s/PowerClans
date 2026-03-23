package com.ie23s.bukkit.plugin.powerclans.database.dto;

import com.ie23s.bukkit.plugin.powerclans.api.IMember;

/**
 * Data transfer object for the {@code clan_members} table.
 */
public record MemberDto(
        String clan,
        String name,
        boolean isModer
) {
    public static MemberDto from(IMember member) {
        return new MemberDto(member.getClan(), member.getName(), member.isModer());
    }
}
