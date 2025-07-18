/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.geyser.entity.type;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket;
import org.geysermc.geyser.entity.EntityDefinition;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.IntEntityMetadata;

import java.util.UUID;

public class TNTEntity extends Entity implements Tickable {
    private int currentTick;

    public TNTEntity(GeyserSession session, int entityId, long geyserId, UUID uuid, EntityDefinition<?> definition, Vector3f position, Vector3f motion, float yaw, float pitch, float headYaw) {
        super(session, entityId, geyserId, uuid, definition, position.add(0, definition.offset(), 0), motion, yaw, pitch, headYaw);
    }

    @Override
    public void moveRelative(double relX, double relY, double relZ, float yaw, float pitch, boolean isOnGround) {
        super.moveRelative(relX, relY + definition.offset(), relZ, yaw, pitch, isOnGround);
    }

    @Override
    public void moveAbsolute(Vector3f position, float yaw, float pitch, float headYaw, boolean isOnGround, boolean teleported) {
        super.moveAbsolute(position.add(Vector3f.from(0, definition.offset(), 0)), yaw, pitch, headYaw, isOnGround, teleported);
    }

    @Override
    public Vector3f position() {
        return this.position.down(definition.offset());
    }

    public void setFuseLength(IntEntityMetadata entityMetadata) {
        currentTick = entityMetadata.getPrimitiveValue();
        setFlag(EntityFlag.IGNITED, true);
        dirtyMetadata.put(EntityDataTypes.FUSE_TIME, currentTick);
    }

    @Override
    public void tick() {
        if (currentTick == 0) {
            // No need to update the fuse when there is none
            return;
        }

        if (currentTick % 5 == 0) {
            dirtyMetadata.put(EntityDataTypes.FUSE_TIME, currentTick);

            SetEntityDataPacket packet = new SetEntityDataPacket();
            packet.setRuntimeEntityId(geyserId);
            packet.getMetadata().put(EntityDataTypes.FUSE_TIME, currentTick);
            session.sendUpstreamPacket(packet);
        }
        currentTick--;
    }
}
