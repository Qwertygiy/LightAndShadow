/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.las;

import org.terasology.core.world.generator.facetProviders.PerlinHumidityProvider;
import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
import org.terasology.core.world.generator.facetProviders.SurfaceToDensityProvider;
import org.terasology.core.world.generator.rasterizers.SolidRasterizer;
import org.terasology.core.world.generator.rasterizers.TreeRasterizer;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.spawner.Spawner;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.staticCities.BlockTheme;
import org.terasology.staticCities.CityWorldGenerator;
import org.terasology.staticCities.DefaultBlockType;
import org.terasology.staticCities.SettlementEntityProvider;
import org.terasology.staticCities.SimpleBiomeProvider;
import org.terasology.staticCities.bldg.BuildingFacetProvider;
import org.terasology.staticCities.blocked.BlockedAreaFacetProvider;
import org.terasology.staticCities.deco.ColumnRasterizer;
import org.terasology.staticCities.deco.DecorationFacetProvider;
import org.terasology.staticCities.deco.SingleBlockRasterizer;
import org.terasology.staticCities.door.DoorFacetProvider;
import org.terasology.staticCities.door.SimpleDoorRasterizer;
import org.terasology.staticCities.door.WingDoorRasterizer;
import org.terasology.staticCities.fences.FenceFacetProvider;
import org.terasology.staticCities.fences.SimpleFenceRasterizer;
import org.terasology.staticCities.flora.FloraFacetProvider;
import org.terasology.staticCities.flora.TreeFacetProvider;
import org.terasology.staticCities.lakes.LakeFacetProvider;
import org.terasology.staticCities.parcels.ParcelFacetProvider;
import org.terasology.staticCities.raster.standard.HollowBuildingPartRasterizer;
import org.terasology.staticCities.raster.standard.RectPartRasterizer;
import org.terasology.staticCities.raster.standard.RoundPartRasterizer;
import org.terasology.staticCities.raster.standard.StaircaseRasterizer;
import org.terasology.staticCities.roads.RoadFacetProvider;
import org.terasology.staticCities.roads.RoadRasterizer;
import org.terasology.staticCities.roof.ConicRoofRasterizer;
import org.terasology.staticCities.roof.DomeRoofRasterizer;
import org.terasology.staticCities.roof.FlatRoofRasterizer;
import org.terasology.staticCities.roof.HipRoofRasterizer;
import org.terasology.staticCities.roof.PentRoofRasterizer;
import org.terasology.staticCities.roof.RoofFacetProvider;
import org.terasology.staticCities.roof.SaddleRoofRasterizer;
import org.terasology.staticCities.settlements.SettlementFacetProvider;
import org.terasology.staticCities.sites.SiteFacetProvider;
import org.terasology.staticCities.surface.InfiniteSurfaceHeightFacetProvider;
import org.terasology.staticCities.surface.SurfaceHeightFacetProvider;
import org.terasology.staticCities.terrain.BuildableTerrainFacetProvider;
import org.terasology.staticCities.walls.TownWallFacetProvider;
import org.terasology.staticCities.walls.TownWallRasterizer;
import org.terasology.staticCities.window.RectWindowRasterizer;
import org.terasology.staticCities.window.SimpleWindowRasterizer;
import org.terasology.staticCities.window.WindowFacetProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.generation.World;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

@RegisterWorldGenerator(id = "las", displayName = "Light & Shadow World")
public class LaSWorldGenerator extends CityWorldGenerator {

    World world;

    private final Spawner spawner = new LaSSpawner();

    @In
    private BlockManager blockManager;

    private BlockTheme theme;

    /**
     * @param uri the uri
     */
    public LaSWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    public Vector3f getSpawnPosition(EntityRef entity) {
        Vector3f pos = spawner.getSpawnPosition(getWorld(), entity);
        return pos != null ? pos : super.getSpawnPosition(entity);
    }

    @Override
    protected WorldBuilder createWorld() {
        int seaLevel = 2;

        theme = BlockTheme.builder(blockManager)
                .register(DefaultBlockType.ROAD_FILL, "CoreBlocks:Dirt")
                .register(DefaultBlockType.ROAD_SURFACE, "CoreBlocks:Gravel")
                .register(DefaultBlockType.LOT_EMPTY, "CoreBlocks:Dirt")
                .register(DefaultBlockType.BUILDING_WALL, "Cities:stonawall1")
                .register(DefaultBlockType.BUILDING_FLOOR, "Cities:stonawall1dark")
                .register(DefaultBlockType.BUILDING_FOUNDATION, "CoreBlocks:Gravel")
                .register(DefaultBlockType.TOWER_STAIRS, "CoreBlocks:CobbleStone")
                .register(DefaultBlockType.ROOF_FLAT, "Cities:rooftiles2")
                .register(DefaultBlockType.ROOF_HIP, "Cities:wood3")
                .register(DefaultBlockType.ROOF_SADDLE, "Cities:wood3")
                .register(DefaultBlockType.ROOF_DOME, "CoreBlocks:Plank")
                .register(DefaultBlockType.ROOF_GABLE, "CoreBlocks:Plank")
                .register(DefaultBlockType.SIMPLE_DOOR, BlockManager.AIR_ID)
                .register(DefaultBlockType.WING_DOOR, BlockManager.AIR_ID)
                .register(DefaultBlockType.WINDOW_GLASS, BlockManager.AIR_ID)
                .register(DefaultBlockType.TOWER_WALL, "Cities:stonawall1")

                // -- requires Fences module
                .registerFamily(DefaultBlockType.FENCE, "Fences:Fence")
                .registerFamily(DefaultBlockType.FENCE_GATE, BlockManager.AIR_ID)  // there is no fence gate :-(
                .registerFamily(DefaultBlockType.TOWER_STAIRS, "CoreBlocks:CobbleStone:engine:stair")
                .registerFamily(DefaultBlockType.BARREL, "StructuralResources:Barrel")
                .registerFamily(DefaultBlockType.LADDER, "CoreBlocks:Ladder")
                .registerFamily(DefaultBlockType.PILLAR_BASE, "CoreBlocks:CobbleStone:StructuralResources:pillarBase")
                .registerFamily(DefaultBlockType.PILLAR_MIDDLE, "CoreBlocks:CobbleStone:StructuralResources:pillar")
                .registerFamily(DefaultBlockType.PILLAR_TOP, "CoreBlocks:CobbleStone:StructuralResources:pillarTop")
                .registerFamily(DefaultBlockType.TORCH, "CoreBlocks:Torch")
                .build();

        PerlinHumidityProvider.Configuration humidityConfig = new PerlinHumidityProvider.Configuration();
        humidityConfig.octaves = 4;
        humidityConfig.scale = 0.5f;

        WorldBuilder worldBuilder = new WorldBuilder(CoreRegistry.get(WorldGeneratorPluginLibrary.class))
                .setSeaLevel(seaLevel)
                .addProvider(new SeaLevelProvider(seaLevel))
                .addProvider(new InfiniteSurfaceHeightFacetProvider())
                .addProvider(new SurfaceHeightFacetProvider())
                .addProvider(new SurfaceToDensityProvider())
                .addProvider(new BuildableTerrainFacetProvider())
                .addProvider(new BlockedAreaFacetProvider())
                .addProvider(new LakeFacetProvider())
                .addProvider(new PerlinHumidityProvider(humidityConfig))
                .addProvider(new SimpleBiomeProvider())
                .addProvider(new SiteFacetProvider())
                .addProvider(new TownWallFacetProvider())
                .addProvider(new RoadFacetProvider())
                .addProvider(new ParcelFacetProvider())
                .addProvider(new FenceFacetProvider())
                .addProvider(new WindowFacetProvider())
                .addProvider(new DecorationFacetProvider())
                .addProvider(new DoorFacetProvider())
                .addProvider(new RoofFacetProvider())
                .addProvider(new BuildingFacetProvider())
                .addProvider(new SettlementFacetProvider())
                .addProvider(new FloraFacetProvider())
                .addProvider(new TreeFacetProvider())
                .addRasterizer(new SolidRasterizer())
                .addPlugins()
                .addEntities(new SettlementEntityProvider())
                .addRasterizer(new RoadRasterizer(theme))
                .addRasterizer(new TownWallRasterizer(theme))
                .addRasterizer(new SimpleFenceRasterizer(theme))
                .addRasterizer(new RectPartRasterizer(theme))
                .addRasterizer(new HollowBuildingPartRasterizer(theme))
                .addRasterizer(new RoundPartRasterizer(theme))
                .addRasterizer(new StaircaseRasterizer(theme))
                .addRasterizer(new FlatRoofRasterizer(theme))
                .addRasterizer(new SaddleRoofRasterizer(theme))
                .addRasterizer(new PentRoofRasterizer(theme))
                .addRasterizer(new HipRoofRasterizer(theme))
                .addRasterizer(new ConicRoofRasterizer(theme))
                .addRasterizer(new DomeRoofRasterizer(theme))
                .addRasterizer(new SimpleWindowRasterizer(theme))
                .addRasterizer(new RectWindowRasterizer(theme))
                .addRasterizer(new SimpleDoorRasterizer(theme))
                .addRasterizer(new WingDoorRasterizer(theme))
                .addRasterizer(new SingleBlockRasterizer(theme))
                .addRasterizer(new ColumnRasterizer(theme))
                .addRasterizer(new LaSFloraRasterizer())
                .addRasterizer(new TreeRasterizer());
        return worldBuilder;
    }
}
