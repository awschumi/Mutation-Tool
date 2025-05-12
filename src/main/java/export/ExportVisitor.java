package export;

import storage.*;

/*
 * The visitor for exporting all of our information contained
 * in the different info (FileInfo, ClassInfo, etc.)
 */
public interface ExportVisitor
{
    String visitFileInfo(FileInfo fi);

    String visitClassInfo(ClassInfo cl);

    String visitMethodInfo(MethodInfo me);

    String visitStatementInfo(StatementInfo st);

    String visitMaskingInfo(MaskingInfo ma);

    String visitPredictionInfo(PredictionInfo pr);

    String visitPositionInfo(PositionInfo po);
}
