package fi.om.municipalityinitiative.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;


/**
 * QFollowInitiative is a Querydsl query type for QFollowInitiative
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QFollowInitiative extends com.mysema.query.sql.RelationalPathBase<QFollowInitiative> {

    private static final long serialVersionUID = -1603606547;

    public static final QFollowInitiative followInitiative = new QFollowInitiative("follow_initiative");

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> initiativeId = createNumber("initiativeId", Long.class);

    public final StringPath unsubscribeHash = createString("unsubscribeHash");

    public final com.mysema.query.sql.PrimaryKey<QFollowInitiative> followInitiativeId = createPrimaryKey(id);

    public final com.mysema.query.sql.ForeignKey<QMunicipalityInitiative> followInitiativeInitiativeId = createForeignKey(initiativeId, "id");

    public QFollowInitiative(String variable) {
        super(QFollowInitiative.class,  forVariable(variable), "municipalityinitiative", "follow_initiative");
        addMetadata();
    }

    public QFollowInitiative(Path<? extends QFollowInitiative> path) {
        super(path.getType(), path.getMetadata(), "municipalityinitiative", "follow_initiative");
        addMetadata();
    }

    public QFollowInitiative(PathMetadata<?> metadata) {
        super(QFollowInitiative.class,  metadata, "municipalityinitiative", "follow_initiative");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(email, ColumnMetadata.named("email").ofType(12).withSize(100).notNull());
        addMetadata(id, ColumnMetadata.named("id").ofType(-5).withSize(19).notNull());
        addMetadata(initiativeId, ColumnMetadata.named("initiative_id").ofType(-5).withSize(19).notNull());
        addMetadata(unsubscribeHash, ColumnMetadata.named("unsubscribe_hash").ofType(12).withSize(40).notNull());
    }

}

