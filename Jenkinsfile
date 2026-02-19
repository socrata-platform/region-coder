@Library('socrata-pipeline-library@9.9.1') _

commonPipeline(
    jobName: 'region-coder',
    language: 'scala',
    projects: [
        [
            name: 'region-coder',
            compiled: true,
            deploymentEcosystem: 'ecs',
            paths: [
                dockerBuildContext: 'docker',
            ],
            type: 'service'
        ]
    ],
    teamsChannelWebhookId: 'WORKFLOW_EGRESS_AUTOMATION',
)
